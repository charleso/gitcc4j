package gitcc.cmd;

public class Reset extends Command {

	@Override
	public void execute() throws Exception {
		String id = "HEAD";
		git.branchForce(config.getCC(), id);
		git.branchForce(config.getCI(), id);
	}
}
