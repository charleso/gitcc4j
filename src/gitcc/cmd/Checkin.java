package gitcc.cmd;

import java.util.List;

import gitcc.cc.Transaction;
import gitcc.git.GitCommit;

public class Checkin extends Command {

	@Override
	public void execute() throws Exception {
		List<GitCommit> log = git.log(config.getCI() + "..");
		if (log.isEmpty())
			return;
		cc.update();
		for (GitCommit c : log) {
			init(new Transaction(c, git.getStatuses(c))).process();
		}
		cc.deliver();
	}
}
