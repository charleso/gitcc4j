package gitcc.cc;

import gitcc.cc.CCFile.Status;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

public class DirDiffTest extends TestCase {

	public void test() {
		List<CCFile> diff = new DirDiffUtil(false).diff("b", f(0), f(1));
		assertEquals(2, diff.size());
		check(diff.get(0), "b/lib", Status.Added);
		check(diff.get(1), "b/a.x", Status.Deleted);
	}

	private void check(CCFile file, String name, Status status) {
		assertEquals(name, file.getFile());
		assertEquals(status, file.getStatus());
	}

	private File f(int name) {
		return new File("test/gitcc/cc/A.compare." + name);
	}
}
