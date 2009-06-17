package gitcc.cmd;

import gitcc.cc.CCCommit;
import gitcc.cc.CCFile;
import gitcc.cc.CCFile.Status;
import gitcc.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class Rebase extends Command {

	@Override
	public void execute() throws Exception {
		cc.rebase();
		String branch = config.getBranch();
		boolean normal = branch != null;
		Date since = normal ? git.getCommitDate(config.getCC()) : null;
		Collection<CCCommit> commits = cc.getHistory(since);
		if (commits.isEmpty())
			return;
		// TODO Git fast import
		if (normal)
			git.checkout(config.getCC());
		for (CCCommit c : commits) {
			handleFiles(c.getFiles());
			git.commit(c);
		}
		if (normal) {
			git.rebase(config.getCI(), config.getCC());
			git.rebase(config.getCC(), branch);
		} else {
			git.branch(config.getCC());
		}
		git.tag(config.getCI(), config.getCC());
	}

	private void handleFiles(List<CCFile> files) throws Exception {
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

	private void add(CCFile f) throws Exception {
		// TODO Full renames
		if (f.getVersion().getFullVersion().length() == 0) {
			System.out.println("IGNORED file: " + f);
			return;
		}
		File newFile = cc.get(f);
		File dest = new File(git.getRoot(), f.getFile());
		if (dest.exists() && !dest.delete())
			throw new RuntimeException("Could not delete file: " + dest);
		dest.getParentFile().mkdirs();
		if (!newFile.renameTo(dest)) {
			try {
				IOUtils.copy(new FileInputStream(newFile),
						new FileOutputStream(dest));
			} finally {
				newFile.delete();
			}
		}
		git.add(f.getFile());
	}

	private void remove(CCFile f) {
		if (new File(git.getRoot(), f.getFile()).exists())
			git.remove(f.getFile());
	}
}
