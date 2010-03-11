package gitcc.cmd;

import gitcc.cc.CCCommit;
import gitcc.cc.CCFile;
import gitcc.cc.CCHistoryParser;
import gitcc.cc.CCFile.Status;
import gitcc.util.ExecException;
import gitcc.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class Rebase extends Command {

	protected static final String BACKUP = ".git/lshistory.bak";
	private static final CCHistoryParser histParser = new CCHistoryParser();

	@Override
	public void execute() throws Exception {
		cc.rebase();
		String branch = config.getBranch();
		boolean normal = branch != null;
		Date since = normal ? getSince() : null;
		Collection<CCCommit> commits = getHistory(since);
		if (commits.isEmpty())
			return;
		// TODO Git fast import
		if (normal)
			git.checkout(config.getCC());
		for (CCCommit c : commits) {
			handleFiles(c.getFiles(), c.getFiles());
			git.commit(c, config.getUser(c.getAuthor()));
		}
		if (normal) {
			doRebase();
		} else {
			git.branch(config.getCC());
		}
		git.branchForce(config.getCI(), config.getCC());
	}

	protected Collection<CCCommit> getHistory(Date since) {
		String lsh = cc.getHistory(since);
		backupHistory(lsh);
		return parseHistory(lsh);
	}

	protected Collection<CCCommit> parseHistory(String lsh) {
		Collection<CCCommit> commits = histParser.parse(lsh, config
				.getBranches());
		for (CCCommit commit : commits) {
			for (CCFile f : commit.getFiles()) {
				cc.fixFile(f);
			}
			commit.setMessage(cc.getRealComment(commit.getMessage()));
		}
		return commits;
	}

	private void backupHistory(String lsh) {
		try {
			FileWriter writer = new FileWriter(BACKUP);
			writer.append(lsh);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected Date getSince() {
		return git.getCommitDate(config.getCC());
	}

	protected void doRebase() {
		git.rebase(config.getCI(), config.getCC());
		git.rebase(config.getCC(), config.getBranch());
	}

	private void handleFiles(List<CCFile> all, List<CCFile> files)
			throws Exception {
		for (CCFile f : files) {
			if (f.getStatus() == Status.Directory) {
				handleFiles(all, cc.diffPred(f));
			} else if (f.getStatus() == Status.Added) {
				add(all, f);
			} else {
				remove(f);
			}
		}
	}

	private void add(List<CCFile> all, CCFile f) throws Exception {
		FileHandler fileHandler = getFile(all, f);
		if (fileHandler.getNewFile() == null)
			return;
		File dest = new File(git.getRoot(), f.getFile());
		dest.getParentFile().mkdirs();
		fileHandler.copyFile(dest);
		try {
			fileHandler.add(f.getFile());
		} catch (ExecException e) {
			if (e.getMessage().contains("The following paths are ignored"))
				return;
			throw e;
		}
	}

	private FileHandler getFile(List<CCFile> all, CCFile f) {
		if (!f.hasVersion()) {
			return new RenameHandler(all, f);
		}
		return new VersionHandler(f);
	}

	private void remove(CCFile f) {
		if (new File(git.getRoot(), f.getFile()).exists()) {
			try {
				git.remove(f.getFile());
			} catch (ExecException e) {
				// Ignore
			}
		}
	}

	private abstract class FileHandler {

		protected File newFile;

		public void setFile(File file) {
			newFile = file;
		}

		public abstract void copyFile(File dest) throws Exception;

		public abstract void add(String dest) throws Exception;

		public File getNewFile() {
			return newFile;
		}
	}

	private class VersionHandler extends FileHandler {

		public VersionHandler(CCFile f) {
			setFile(cc.get(f));
		}

		@Override
		public void copyFile(File dest) throws Exception {
			if (dest.exists() && !dest.delete())
				throw new RuntimeException("Could not delete file: " + dest);
			if (!newFile.renameTo(dest)) {
				try {
					IOUtils.copy(new FileInputStream(newFile),
							new FileOutputStream(dest));
				} finally {
					newFile.delete();
				}
			}
		}

		@Override
		public void add(String file) throws Exception {
			git.add(file);
		}
	}

	private class RenameHandler extends FileHandler {

		public RenameHandler(List<CCFile> all, CCFile f) {
			setFile(handleRename(all, f));
		}

		private File handleRename(List<CCFile> all, CCFile f) {
			for (CCFile file : all) {
				if (file.getFile().equals(f.getFile())) {
					return null;
				}
			}

			// This is the lazy approach, but 9 times out of 10 it'll be fine.
			// The probably is that the git history will have a slight anomaly.
			// The important thing is that the working tree will be correct by
			// the
			// end of this rebase.
			File newFile = cc.toFile(f.getFile());
			return newFile.exists() ? newFile : null;
		}

		@Override
		public void copyFile(File dest) throws Exception {
			recurse(newFile, dest);
		}

		private void recurse(File src, File dest) throws Exception {
			if (src.isDirectory()) {
				dest.mkdir();
				for (File file : src.listFiles()) {
					if (file.getName().equals(".copyarea.db"))
						continue;
					recurse(file, new File(dest, file.getName()));
				}
			} else {
				IOUtils.copy(new FileInputStream(src), new FileOutputStream(
						dest));
			}
		}

		@Override
		public void add(String file) throws Exception {
			git.add(file);
		}
	}
}
