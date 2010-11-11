package gitcc.git;

import gitcc.git.FileStatus.Status;

import java.util.List;
import java.util.ListIterator;

public class IgnoreLevel {

	private static final String SEP = "/";
	private final int ignoreLevel;

	public IgnoreLevel(int ignoreLevel) {
		this.ignoreLevel = ignoreLevel;
	}

	public List<FileStatus> filter(List<FileStatus> log) {
		for (ListIterator<FileStatus> i = log.listIterator(); i.hasNext();) {
			FileStatus status = i.next();
			String file = status.getFile();
			if (status.getStatus() == Status.Renamed) {
				String old = status.getOldFile();
				if (!getPath(file).equals(getPath(old))) {
					i.remove();
					i.add(new FileStatus(old, Status.Deleted));
					i.add(new FileStatus(file, Status.Added));
					continue;
				}
			} else if (file.split(SEP).length <= ignoreLevel) {
				i.remove();
			}
		}
		return log;
	}

	private String getPath(String path) {
		String[] split = path.split(SEP);
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < ignoreLevel; i++) {
			b.append(split[i]).append(SEP);
		}
		return b.toString();
	}
}
