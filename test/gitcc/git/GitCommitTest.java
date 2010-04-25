package gitcc.git;

import junit.framework.TestCase;

public class GitCommitTest extends TestCase {

	public void testGetSubject() {
		assertEquals("", GitCommit.getSubject(""));
		assertEquals("", GitCommit.getSubject("\n"));
	}
}
