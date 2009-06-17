package gitcc.cc;

import gitcc.config.Config;
import gitcc.git.FileStatus;
import gitcc.git.Git;
import gitcc.git.GitCommit;
import gitcc.git.FileStatus.Status;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

public class TransactionTest extends TestCase {

	public void test() {
		GitCommit commit = new GitCommit();
		commit.setId("sha");
		String message = "x\ny";
		commit.setMessage(message);
		List<FileStatus> statuses = Arrays.asList(f("a", Status.Added), f("d",
				Status.Deleted), f("m", Status.Modified), new FileStatus("r2",
				Status.Renamed, "r1"));
		Transaction t = new Transaction(commit, statuses);
		IMocksControl ctrl = EasyMock.createStrictControl();
		Clearcase cc = t.cc = ctrl.createMock(Clearcase.class);
		Git git = t.git = ctrl.createMock(Git.class);
		Config config = t.config = new Config();
		config.setBranch("my_branch");
		EasyMock.expect(cc.mkact("x")).andReturn("act");
		cc.checkout(".");
		cc.checkout("m");
		cc.checkout("r1");
		write(git, cc, "a");
		cc.add("a", message);
		cc.delete("d");
		write(git, cc, "m");
		cc.move("r1", "r2");
		write(git, cc, "r2");
		cc.checkin(".", message);
		cc.checkin("m", message);
		cc.checkin("r2", message);
		git.tag("my_branch_ci", "sha");
		ctrl.replay();
		t.process();
		ctrl.verify();
	}

	private void write(Git git, Clearcase cc, String a) {
		EasyMock.expect(git.catFile("sha", a)).andReturn(null);
		cc.write(a, null);
	}

	private FileStatus f(String file, Status status) {
		return new FileStatus(file, status);
	}
}
