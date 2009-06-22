package gitcc.cmd;

import gitcc.util.ExecException;

public class DaemonTest extends AbstractDaemonTest {

	public void test() throws Exception {
		int passes = 10;
		for (int i = 0; i < passes; i++) {
			singlePass();
		}
	}

	public void testBadMerge() throws Exception {
		git.checkout("master");
		add(git, GIT_FILE);
		git.checkout("master_cc");
		try {
			singlePass();
			fail("Where is our merge conflict?!?");
		} catch (ExecException e) {
			assertTrue(e.getMessage().contains("Merge conflict"));
			assertEquals("master_cc", git.getBranch());
			assertEquals("", git.exec("ls-files", "-m"));
		}
		assertFalse(daemon.sanityCheck("master_cc", "master"));
	}
}
