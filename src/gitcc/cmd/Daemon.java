package gitcc.cmd;

import gitcc.Log;
import gitcc.git.GitCommit;
import gitcc.util.CheckinException;
import gitcc.util.EmailUtil;

import java.util.Arrays;
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

	private EmailUtil email;

	@Override
	public void init() {
		email = new EmailUtil(config);
		git.checkout(config.getCC());
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
				}
			} else {
				String error = "Repository is in a bad state. Wake me up when you fix it.";
				Log.info(error);
			}
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
		checkin();
		rebase();
	}

	private void checkin() throws Exception {
		try {
			exec(new Checkin() {
				@Override
				protected void checkin(List<GitCommit> log) {
					GitCommit c = log.get(0);
					cc = cc.cloneForUser(config.getUser(c.getAuthor()));
					// Only checkin one at a time so we can change users
					super.checkin(Arrays.asList(c));
				}
			});
		} catch (CheckinException e) {
			// Basically ignore this - we don't care
			Log.debug(e.getMessage());
		}
	}

	private void rebase() throws Exception {
		exec(new Rebase() {
			@Override
			protected void doRebase() {
				String branch_cc = config.getCC();
				try {
					git.rebase(config.getCI(), branch_cc);
					String branch = config.getBranch();
					if (sanityCheck(branch, branch_cc)) {
						git.checkout(branch);
						git.merge(branch_cc);
					}
				} finally {
					git.checkoutForce(branch_cc);
				}
			}
		});
	}

	private void exec(Command cmd) throws Exception {
		init(cmd).execute();
	}
}
