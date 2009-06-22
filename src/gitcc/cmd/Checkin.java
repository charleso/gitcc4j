package gitcc.cmd;

import gitcc.cc.Credentials;
import gitcc.cc.Transaction;
import gitcc.git.GitCommit;

import java.util.List;

public class Checkin extends Command {

	@Override
	public void execute() throws Exception {
		List<GitCommit> log = git.log(config.getCI() + "..");
		if (log.isEmpty())
			return;
		cc.update();
		String range = config.getCI() + ".." + config.getBranch();
		for (GitCommit c : git.log(range)) {
			Credentials credentials = new Credentials(c.getAuthor(), config
					.getUser(c.getAuthor()).getPassword(), config.getGroup());
			Transaction t = init(new Transaction(c, git.getStatuses(c)));
			t.cc = cc.getSession(credentials);
			t.process();
		}
		cc.deliver();
	}
}
