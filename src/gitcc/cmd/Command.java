package gitcc.cmd;

import gitcc.Common;

public abstract class Command extends Common {

	public abstract void execute() throws Exception;

	public void init() {
		// Do nothing
	}

}
