package gitcc.cmd;

import static org.easymock.EasyMock.expect;
import gitcc.cc.CCCommit;
import gitcc.cc.CCFile;
import gitcc.cc.Clearcase;
import gitcc.cc.CCFile.Status;
import gitcc.config.Config;
import gitcc.config.User;
import gitcc.git.GitImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import junit.framework.TestCase;

import org.easymock.EasyMock;

public abstract class AbstractDaemonTest extends TestCase {

	protected static final String GIT_FILE = "a";

	private File root;
	private Clearcase cc;
	protected Daemon daemon;
	protected GitImpl git;

	@Override
	protected void setUp() throws Exception {
		root = new File(System.getProperty("java.io.tmpdir"), "deleteme");
		daemon = new Daemon();
		Config config = daemon.config = new Config();
		config.setBranch("master");
		cc = daemon.cc = EasyMock.createNiceMock(Clearcase.class);
		daemon.git = git = newGit("repo1", config);
		git.exec("init");
		git.exec("commit", "--allow-empty", "-m", "Empty");
		git.branchForce("master_ci", "HEAD");
		git.branch("master_cc");
	}

	protected GitImpl newGit(String name, Config config) {
		File f = new File(root, name);
		f.mkdirs();
		return new GitImpl(f);
	}

	@Override
	protected void tearDown() throws Exception {
		delete(root);
	}

	private void delete(File dir) {
		if (dir.isDirectory()) {
			for (File child : dir.listFiles()) {
				delete(child);
			}
		}
		dir.delete();
	}

	protected void singlePass() throws Exception {
		_singlePass();
		EasyMock.replay(cc);
		daemon.singlePass();
		EasyMock.reset(cc);
	}

	private void _singlePass() throws Exception {
		CCCommit commit = new CCCommit("auth1@test", new Date(), "cc");
		User user = (User) EasyMock.anyObject();
		EasyMock.expect(cc.cloneForUser(user)).andReturn(cc);
		Date date = EasyMock.anyObject();
		expect(cc.getHistory(date)).andReturn(Arrays.asList(commit));
		get(cc, commit);
	}

	private void get(Clearcase cc, CCCommit commit) throws Exception {
		CCFile file = new CCFile(GIT_FILE, "/main/0", Status.Added);
		commit.getFiles().add(file);
		File tmp = File.createTempFile("abc", "tmp");
		tmp.deleteOnExit();
		write(tmp);
		expect(cc.get(file)).andReturn(tmp);
	}

	protected void add(GitImpl git, String file) {
		write(new File(git.getRoot(), file));
		git.add(file);
		User user = new User("a@example.com");
		git.commit(new CCCommit("auth2@test", new Date(), "git"), user);
	}

	private void write(File tmp) {
		try {
			FileOutputStream out = new FileOutputStream(tmp);
			out.write(UUID.randomUUID().toString().getBytes());
			out.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
