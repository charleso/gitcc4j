package gitcc.cmd;

import gitcc.cc.Transaction;
import gitcc.git.GitCommit;

public class Checkin extends Command {

	@Override
	public void execute() throws Exception {
		cc.update();
		for (GitCommit c : git.log(config.getCI() + "..")) {
			init(new Transaction(c, git.getStatuses(c))).process();
		}
		cc.deliver();
	}
}
