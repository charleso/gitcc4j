package gitcc.git;

import gitcc.git.FileStatus.Status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GitUtil {

	public List<GitCommit> parseLog(String result) {
		List<GitCommit> log = new ArrayList<GitCommit>();
		for (String line : result.split("\0")) {
			if (line.length() == 0)
				break;
			log.add(_parseLog(line));
		}
		return log;
	}

	private GitCommit _parseLog(String line) {
		String[] s = line.split("\1");
		return new GitCommit(s[0], s[1], s[2].trim());
	}

	public String parseLsTree(String blob) {
		return blob.split(" ")[2].substring(0, 40);
	}

	public List<FileStatus> parseDiff(String result) {
		List<FileStatus> log = new ArrayList<FileStatus>();
		if (result.isEmpty())
			return log;
		String[] split = result.split("\0");
		for (int i = 0; i < split.length; i++) {
			String type = split[i];
			String file = split[++i];
			String old = file;
			Status s = null;
			switch (type.charAt(0)) {
			case 'C':
				file = split[++i];
			case 'A':
				s = Status.Added;
				break;
			case 'D':
				s = Status.Deleted;
				break;
			case 'M':
				s = Status.Modified;
				break;
			case 'R':
				s = Status.Renamed;
				file = split[++i];
				break;
			}
			log.add(new FileStatus(file, s, old));
		}
		return log;
	}

	public Date parseDate(String result) {
		return new Date(Long.parseLong(result));
	}

	public String parseBranch(String result) {
		for (String line : result.split("\n")) {
			if (line.startsWith("*"))
				return line.substring(2);
		}
		return null;
	}

	public static List<List<GitCommit>> splitLogByUser(List<GitCommit> log) {
		List<List<GitCommit>> l = new ArrayList<List<GitCommit>>();
		String last = null;
		int i = -1;
		for (GitCommit c : log) {
			if (!c.getAuthor().equals(last)) {
				l.add(new ArrayList<GitCommit>());
				last = c.getAuthor();
				i++;
			}
			l.get(i).add(c);
		}
		return l;
	}
}
