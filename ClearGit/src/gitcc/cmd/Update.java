package gitcc.cmd;

public class Update extends Daemon {

	@Override
	public void execute() throws Exception {
		singlePass();
	}
}
