package gitcc.cmd;

import gitcc.cc.Transaction;
import gitcc.git.FileStatus;
import gitcc.git.GitCommit;
import gitcc.git.IgnoreLevel;

import java.util.List;

public class Checkin extends Command {

	private IgnoreLevel ignoreLevel;

	@Override
	public void init() {
		ignoreLevel = new IgnoreLevel(config.getIgnoreLevel());
	}

	@Override
	public void execute() throws Exception {
		String range = config.getCI() + ".." + config.getBranch();
		List<GitCommit> log = git.log(range);
		if (log.isEmpty())
			return;
		checkin(log);
	}

	protected void checkin(List<GitCommit> log) throws Exception {
		cc.update();
		int count = 0;
		for (GitCommit c : log) {
			count = checkin(c, count);
			git.branchForce(config.getCI(), c.getId());
		}
		if (count > 0 && config.isDeliver()) {
			cc.deliver();
			makeBaseline();
		}
	}

	private int checkin(GitCommit c, int count) {
		if (c.getMessage().startsWith("***"))
			return count;
		List<FileStatus> statuses = git.getStatuses(c);
		statuses = ignoreLevel.filter(statuses);
		if (!statuses.isEmpty()) {
			init(new Transaction(c, statuses)).process();
			count++;
		}
		return count;
	}

	protected void makeBaseline() {
		cc.makeBaseline();
	}
}
