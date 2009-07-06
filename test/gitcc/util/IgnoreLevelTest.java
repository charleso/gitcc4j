package gitcc.util;

import gitcc.git.FileStatus;
import gitcc.git.IgnoreLevel;
import gitcc.git.FileStatus.Status;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class IgnoreLevelTest extends TestCase {

	private List<FileStatus> log = new ArrayList<FileStatus>();

	private void filter(int level, int expected) {
		log = new IgnoreLevel(level).filter(log);
		assertEquals(expected, log.size());
	}

	public void test() {
		filter(1, 0, "a");
		filter(1, 3, "a/b");
		filter(2, 0, "a/b");
		filter(2, 3, "a/b/c");
	}

	private void filter(int level, int expected, String path) {
		log.add(new FileStatus(path, Status.Added));
		log.add(new FileStatus(path, Status.Deleted));
		log.add(new FileStatus(path, Status.Modified));
		filter(level, expected);
		log.clear();
	}

	public void testRename() {
		rename(1, false, "a/b", "a/c");
		rename(1, true, "a/b", "c/d");
		rename(2, false, "a/b/c", "a/b/d");
		rename(2, true, "a/b/c", "a/d/e");
	}

	private void rename(int level, boolean test, String old, String file) {
		log.clear();
		log.add(new FileStatus(file, Status.Renamed, old));
		filter(level, test ? 2 : 1);
		if (test) {
			assertEquals(Status.Deleted, log.get(0).getStatus());
			assertEquals(old, log.get(0).getFile());
			assertEquals(Status.Added, log.get(1).getStatus());
			assertEquals(file, log.get(1).getFile());
		}
	}
}
