package gitcc.cmd;

import gitcc.cc.CCCommit;
import gitcc.cc.CCFile;
import gitcc.cc.CCFile.Status;
import gitcc.git.FastImport;
import gitcc.git.Import;
import gitcc.util.ExecException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class Rebase extends Command {

	private Import imp;

	@Override
	public void execute() throws Exception {
		cc.rebase();
		String branch = config.getBranch();
		boolean normal = branch != null;
		Date since = normal ? getSince() : null;
		Collection<CCCommit> commits = cc.getHistory(since);
		if (commits.isEmpty())
			return;
		doImport(normal, commits);
		if (normal) {
			doRebase();
		} else {
			git.branch(config.getCC());
		}
		git.branchForce(config.getCI(), config.getCC());
	}

	private void doImport(boolean normal, Collection<CCCommit> commits) {
		imp = new FastImport(git.fastImport());
		try {
			if (normal)
				imp.checkout(config.getCC());
			for (CCCommit c : commits) {
				handleFiles(c.getFiles());
				imp.commit(c, config.getUser(c.getAuthor()));
			}
		} finally {
			imp.close();
		}
	}

	protected Date getSince() {
		return git.getCommitDate(config.getCC());
	}

	protected void doRebase() {
		git.rebase(config.getCI(), config.getCC());
		git.rebase(config.getCC(), config.getBranch());
	}

	private void handleFiles(List<CCFile> files) {
		for (CCFile f : files) {
			if (f.getStatus() == Status.Directory) {
				handleFiles(cc.diffPred(f));
			} else if (f.getStatus() == Status.Added) {
				add(f);
			} else {
				remove(f);
			}
		}
	}

	private void add(CCFile f) {
		final File newFile = getFile(f);
		if (!newFile.exists())
			return;
		recurse(cc.getRoot(), f.getFile(), new Recurser() {
			@Override
			public void handleFile(String path) {
				try {
					InputStream in = new FileInputStream(newFile);
					imp.add(path, newFile.length(), in);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					newFile.delete();
				}
			}
		});
	}

	private File getFile(CCFile f) {
		if (!f.hasVersion()) {
			// This is the lazy approach, but 9 times out of 10 it'll be fine.
			// The probably is that the git history will have a slight anomaly.
			// The important thing is that the working tree will be correct by
			// the end of this rebase.
			return cc.toFile(f.getFile());
		}
		return cc.get(f);
	}

	private void remove(CCFile f) {
		recurse(git.getRoot(), f.getFile(), new Recurser() {
			@Override
			public void handleFile(String path) {
				try {
					imp.remove(path);
				} catch (ExecException e) {
					// Ignore
				}
			}
		});
	}

	private void recurse(File root, String path, Recurser r) {
		File file = new File(root, path);
		if (file.isDirectory()) {
			for (String child : file.list()) {
				recurse(root, path + '/' + child, r);
			}
		} else {
			r.handleFile(path);
		}
	}

	private interface Recurser {
		void handleFile(String path);
	}
}
