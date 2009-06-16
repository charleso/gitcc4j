package gitcc;

import gitcc.cc.Clearcase;
import gitcc.config.Config;
import gitcc.git.Git;

public class Common {

	public Git git;
	public Clearcase cc;
	public Config config;

	public <T extends Common> T init(T c) {
		c.git = git;
		c.cc = cc;
		c.config = config;
		return c;
	}
}
