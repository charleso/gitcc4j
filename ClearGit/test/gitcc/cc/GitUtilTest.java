package gitcc.cc;

import gitcc.git.FileStatus;
import gitcc.git.GitCommit;
import gitcc.git.GitUtil;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class GitUtilTest extends TestCase {

	private GitUtil util = new GitUtil();

	public void testParseLogEmpty() {
		assertTrue(util.parseLog("").isEmpty());
	}

	public void testParseLog() {
		String e = "abc\1user1\1Snapshot\n\0def\1user2\1Cooking\n\0ghi\1user3\1Initial";
		List<GitCommit> commits = util.parseLog(e);
		assertEquals(3, commits.size());
		assertEquals("abc", commits.get(0).getId());
		assertEquals("user1", commits.get(0).getAuthor());
		assertEquals("Snapshot", commits.get(0).getMessage());
	}

	public void testParseLsTree() {
		String sha = "54586b18b6319c6252f9c0725399f1899db32ecd";
		assertEquals(sha, util.parseLsTree("100644 blob " + sha + "\tfile"));
	}

	public void testParseDiff() {
		List<FileStatus> s = util
				.parseDiff("A\0a\0D\0d\0M\0m\0R\0r1\0r2\0C\0c1\0c2");
		StringBuilder b = new StringBuilder();
		for (FileStatus fs : s) {
			b.append(fs.getStatus().toString().charAt(0));
			b.append("|");
			b.append(fs.getOldFile());
			b.append("|");
			b.append(fs.getFile());
		}
		assertEquals("A|a|aD|d|dM|m|mR|r1|r2A|c1|c2", b.toString());
	}

	public void testParseDate() {
		assertEquals(1244881431, util.parseDate("1244881431").getTime());
	}

	public void testParseBranch() {
		assertEquals("master", util.parseBranch("* master\n  test\n"));
	}

	public void testSplitLogByUser() {
		List<GitCommit> log = new ArrayList<GitCommit>();
		log.add(new GitCommit(null, "a", null));
		log.add(new GitCommit(null, "a", null));
		log.add(new GitCommit(null, "b", null));
		log.add(new GitCommit(null, "b", null));
		log.add(new GitCommit(null, "c", null));
		log.add(new GitCommit(null, "b", null));
		log.add(new GitCommit(null, "a", null));
		List<List<GitCommit>> l2 = GitUtil.splitLogByUser(log);
		String s = "";
		for (List<GitCommit> l : l2) {
			for (GitCommit c : l) {
				s += c.getAuthor();
			}
			s += "|";
		}
		assertEquals("aa|bb|c|b|a|", s);
	}
}
