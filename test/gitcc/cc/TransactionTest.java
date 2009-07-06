package gitcc.cc;

import gitcc.config.Config;
import gitcc.git.FileStatus;
import gitcc.git.Git;
import gitcc.git.GitCommit;
import gitcc.git.FileStatus.Status;
import gitcc.util.CheckinException;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

public class TransactionTest extends TestCase {

	private Clearcase cc;
	private Git git;
	private Config config;
	private String message = "x\ny";
	private IMocksControl ctrl;
	private Transaction t;

	private void setup(List<FileStatus> statuses) {
		GitCommit commit = new GitCommit();
		commit.setId("sha");
		commit.setMessage(message);
		t = new Transaction(commit, statuses);
		ctrl = EasyMock.createStrictControl();
		cc = t.cc = ctrl.createMock(Clearcase.class);
		git = t.git = ctrl.createMock(Git.class);
		config = t.config = new Config();
		config.setBranch("my_branch");
		EasyMock.expect(cc.mkact("x")).andReturn("act");
		EasyMock.expect(git.mergeBase("my_branch_ci", "HEAD"))
				.andReturn("base");
	}

	private void process() {
		ctrl.replay();
		try {
			t.process();
		} finally {
			ctrl.verify();
		}
	}

	public void test() {
		setup(Arrays.asList(f("a", Status.Added), f("d", Status.Deleted), f(
				"m", Status.Modified), new FileStatus("r2", Status.Renamed,
				"r1")));
		cc.checkout(".");
		stage("m");
		stage("r1");
		write("a");
		cc.add("a", message);
		cc.delete("d");
		write("m");
		cc.move("r1", "r2");
		write("r2");
		cc.checkin(".", message);
		cc.checkin("m", message);
		cc.checkin("r2", message);
		process();
	}

	public void testFail() {
		setup(Arrays.asList(f("m", Status.Modified)));
		stage("m", "a", "b");
		cc.uncheckout("m");
		cc.rmact("act");
		try {
			process();
			fail();
		} catch (CheckinException e) {
			// Ignore
		}
	}

	private void stage(String file) {
		String hash = "hash";
		stage(file, hash, hash);
	}

	private void stage(String file, String hash1, String hash2) {
		cc.checkout(file);
		EasyMock.expect(cc.toFile(file)).andReturn(new File("/" + file));
		EasyMock.expect(git.hashObject("/" + file)).andReturn(hash1);
		EasyMock.expect(git.getBlob(file, "base")).andReturn(hash2);
	}

	private void write(String a) {
		EasyMock.expect(git.catFile("sha", a)).andReturn(null);
		cc.write(a, null);
	}

	private FileStatus f(String file, Status status) {
		return new FileStatus(file, status);
	}
}
