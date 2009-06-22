package gitcc.cmd;

import gitcc.config.Config;
import gitcc.git.GitImpl;
import gitcc.util.ExecException;

import java.util.Random;

/**
 * This is more a test of git than anything.
 * <p>
 * TODO Fix - currently need to comment out the transaction in Checkin
 */
public class ThreadedDaemonTest extends AbstractDaemonTest {

	private static final int ITERATIONS = 100;

	public void testThreading() throws Exception {
		daemon.init();
		git.exec("clone", ".", "../repo2");
		final GitImpl git2 = newGit("repo2", new Config());
		// Client
		DThread t1 = new DThread() {
			@Override
			public void _run() throws Exception {
				add(git2, "b");
				try {
					git2.exec("push", "origin", "master");
				} catch (ExecException e) {
					git2.exec("pull", "--rebase");
				}
			}
		};
		// Server
		DThread t2 = new DThread() {
			@Override
			public void _run() throws Exception {
				singlePass();
			}
		};
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		t1.test();
		t2.test();
	}

	private abstract class DThread extends Thread {

		private Exception e;

		@Override
		public void run() {
			try {
				for (int i = 0; i < ITERATIONS; i++) {
					_run();
					Thread.sleep(1000 + new Random().nextInt(1000));
				}
			} catch (Exception ex) {
				e = ex;
			}
		}

		public abstract void _run() throws Exception;

		public void test() throws Exception {
			if (e != null) {
				throw e;
			}
		}
	}
}
