package gitcc.git;

import gitcc.git.FileStatus.Status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GitUtil {

	public List<GitCommit> parseLog(String result) {
		List<GitCommit> log = new ArrayList<GitCommit>();
		for (String line : result.split("\0")) {
			log.add(_parseLog(line));
		}
		return log;
	}

	private GitCommit _parseLog(String line) {
		GitCommit commit = new GitCommit();
		String[] s = line.split("\1");
		commit.setId(s[0]);
		commit.setMessage(s[1].trim());
		return commit;
	}

	public String parseLsTree(String blob) {
		return blob.split(" ")[2].substring(0, 40);
	}

	public List<FileStatus> parseDiff(String result) {
		List<FileStatus> log = new ArrayList<FileStatus>();
		String[] split = result.split("\0");
		for (int i = 0; i < split.length; i++) {
			String type = split[i];
			String file = split[++i];
			String old = file;
			Status s = null;
			switch (type.charAt(0)) {
			case 'A':
			case 'C':
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
}
