package gitcc.cc;

import gitcc.Common;
import gitcc.git.FileStatus;
import gitcc.git.GitCommit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Transaction extends Common {

	protected Set<String> checkouts = new LinkedHashSet<String>();
	protected List<String> dirs = new ArrayList<String>();
	private final String id;
	private final String commit;
	private final List<FileStatus> statuses;
	private String activity;

	public Transaction(GitCommit commit, List<FileStatus> statuses) {
		this.id = commit.getId();
		this.commit = commit.getMessage();
		this.statuses = statuses;
	}

	public void process() {
		activity = cc.mkact(GitCommit.getSubject(commit));
		try {
			phase1();
			mkdirs();
		} catch (RuntimeException e) {
			rollback();
			throw e;
		}
		phase2();
		commit();
	}

	private void phase1() {
		for (FileStatus s : statuses) {
			String file = s.getFile();
			switch (s.getStatus()) {
			case Added:
				stageDir(file);
				break;
			case Deleted:
				stageDir(file);
				break;
			case Renamed:
				checkout(s.getOldFile());
				stageDir(s.getOldFile());
				stageDir(file);
				break;
			case Modified:
				checkout(s.getFile());
				break;
			}
		}
	}

	private void checkout(String oldFile) {
		if (checkouts.contains(oldFile))
			return;
		cc.checkout(oldFile);
		checkouts.add(oldFile);
	}

	private void stageDir(String f) {
		File dir = new File(f).getParentFile();
		while (dir != null && !cc.exists(dir.getPath())) {
			dirs.add(dir.getPath());
			dir = dir.getParentFile();
		}
		checkout(dir == null ? "." : dir.getPath());
	}

	private void rollback() {
		for (String f : checkouts) {
			cc.uncheckout(f);
		}
		cc.rmact(activity);
	}

	private void phase2() {
		for (FileStatus s : statuses) {
			switch (s.getStatus()) {
			case Added:
				write(s);
				cc.add(s.getFile());
				break;
			case Deleted:
				cc.delete(s.getFile());
				break;
			case Renamed:
				String oldFile = s.getOldFile();
				String file = s.getFile();
				cc.move(oldFile, file);
				checkouts.remove(oldFile);
				checkouts.add(file);
				write(s);
				break;
			case Modified:
				write(s);
				break;
			}
		}
	}

	private void mkdirs() {
		Collections.reverse(dirs);
		for (String dir : dirs) {
			if (!cc.exists(dir)) {
				cc.mkdir(dir);
				checkouts.add(dir);
			}
		}
	}

	private void commit() {
		for (String f : checkouts) {
			cc.checkin(f, commit);
		}
		git.tag(config.getCI(), id);
	}

	private void write(FileStatus s) {
		cc.write(s.getFile(), git.catFile(id, s.getFile()));
	}
}
