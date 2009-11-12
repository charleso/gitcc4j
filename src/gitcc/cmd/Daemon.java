package gitcc.cmd;

import gitcc.Log;
import gitcc.config.Config;
import gitcc.config.User;
import gitcc.git.GitCommit;
import gitcc.git.GitUtil;
import gitcc.util.CheckinException;
import gitcc.util.EmailUtil;

import java.util.Date;
import java.util.List;

/**
 * At the moment this is a simple loop of checkin + rebase + sleep.
 * <p>
 * At any stage if something goes wrong, most importantly a merge conflict, we
 * should instantly try to send for help and wait. Don't try to get smart.
 * <p>
 * The important part occurs for each rebase. We have to consider the
 * possibility that someone will decide to push at exactly the same time. My
 * solution at the moment is to use receive.denyCurrentBranch to (hopefully)
 * stop this happening. The other alternative might be not let git do _any_
 * merges and instead try to push to Clearcase and then rebase. I figure that
 * this shouldn't happen too often either way and we don't really want to have
 * to constantly tend to the daemon at every minor conflict.
 */
public class Daemon extends Command {

	private static final String SINCE = "gitcc.since";

	private EmailUtil email;

	@Override
	public void init() {
		email = new EmailUtil(config);
		git.checkout(Config.DEFAULT_MASTER);
		git.setConfig("receive.denyCurrentBranch", "true");
		git.setConfig("receive.denyNonFastForwards", "true");
	}

	@Override
	public void execute() throws Exception {
		while (true) {
			if (sanityCheck(config.getCC(), config.getBranch())) {
				try {
					singlePass();
				} catch (Exception e) {
					e.printStackTrace();
					email.send(e);
					System.exit(1);
				}
			} else {
				String error = "Repository is in a bad state. Wake me up when you fix it.";
				Log.info(error);
			}
			Log.info("Sleeping...");
			Thread.sleep(config.getSleep());
		}
	}

	/**
	 * Will only happen if our merge went bad and we haven't fixed it.
	 */
	protected boolean sanityCheck(String b1, String b2) {
		String cc = git.getId(b1);
		String base = git.mergeBase(b1, b2);
		return base.equals(cc);
	}

	protected void singlePass() throws Exception {
		pull();
		checkin();
		rebase();
		push();
	}

	private void checkin() throws Exception {
		try {
			exec(new Checkin() {
				@Override
				protected void checkin(List<GitCommit> log) throws Exception {
					// Only checkin one at a time so we can change users
					for (List<GitCommit> l : GitUtil.splitLogByUser(log)) {
						GitCommit c = l.get(0);
						User user = config.getUserByEmail(c.getAuthor());
						if (user == null)
							throw new RuntimeException("User not found: "
									+ c.getAuthor());
						cc = cc.cloneForUser(user);
						super.checkin(l);
					}
				}

				@Override
				protected void makeBaseline() {
					// TODO Permission problems
					Daemon.this.cc.makeBaseline();
				}
			});
		} catch (CheckinException e) {
			// Basically ignore this - we don't care
			Log.debug(e.getMessage());
		}
	}

	private void rebase() throws Exception {
		git.checkout(config.getCC());
		exec(new Rebase() {

			private long since;

			@Override
			protected void doRebase() {
				String branch_cc = config.getCC();
				try {
					rebase(branch_cc);
					String branch = config.getBranch();
					if (sanityCheck(branch, branch_cc)) {
						git.checkout(branch);
						git.merge(branch_cc);
					}
					git.setConfig(SINCE, Long.toString(since));
					git.gc();
				} finally {
					git.checkoutForce(branch_cc);
				}
			}

			/**
			 * ClearCase may have a slightly different version of events if a
			 * file was modified and then deleted. By comparing the tree we can
			 * avoid unnecessary rebase errors.
			 */
			private void rebase(String branch_cc) {
				String ci = config.getCI();
				if (git.diffTree(ci, branch_cc).length() == 0) {
					git.resetHard(ci);
				} else {
					git.rebase(ci, branch_cc);
				}
			}

			@Override
			protected Date getSince() {
				Date lastSince = null;
				try {
					lastSince = new Date(Long.parseLong(git.getConfig(SINCE)));
				} catch (Exception e) {
					// Ignore
				}
				since = new Date().getTime();
				return lastSince != null ? lastSince : super.getSince();
			}
		});
	}

	private void pull() {
		if (config.getRemote() != null) {
			git.checkout(config.getBranch());
			git.pullRebase(config.getRemote());
		}
	}

	private void push() {
		if (config.getRemote() != null) {
			git.checkout(config.getBranch());
			git.push(config.getRemote());
		}
	}

	private void exec(Command cmd) throws Exception {
		init(cmd);
		cmd.init();
		cmd.execute();
	}
}
