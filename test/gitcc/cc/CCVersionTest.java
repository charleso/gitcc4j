package gitcc.cc;

import junit.framework.TestCase;

public class CCVersionTest extends TestCase {

	public void testGetBranch() {
		assertEquals("z", new CCVersion("/x/y/z/0").getBranch());
	}

	public void testGetBranches() {
		String[] xxx = new CCVersion("/x/y/z/0").getBranches();
		assertEquals(3, xxx.length);
		assertEquals("x", xxx[0]);
		assertEquals("z", xxx[2]);
	}
}
