package gitcc.cmd;

import gitcc.cc.Transaction;
import gitcc.git.GitCommit;

import java.util.List;

public class Checkin extends Command {

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
		for (GitCommit c : log) {
			init(new Transaction(c, git.getStatuses(c))).process();
			git.tag(config.getCI(), c.getId());
		}
		cc.deliver();
		makeBaseline();
	}

	protected void makeBaseline() {
		cc.makeBaseline();
	}
}
