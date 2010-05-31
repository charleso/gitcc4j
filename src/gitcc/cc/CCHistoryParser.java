package gitcc.cc;

import static gitcc.git.GitCommit.getSubject;
import gitcc.cc.CCFile.Status;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CCHistoryParser {

	private static final String SINCE_DATE_FORMAT = "dd-MMM-yyyy.HH:mm:ss";
	private static final String LSH_FORMAT = "%o%m|%Nd|%u|%En|%Vn|";

	private static final String FILE = "checkinversion";
	private static final String DIR = "checkindirectory version";
	public static final String SEP = "@@@";
	private static final String DATE_FORMAT = "yyyyMMdd.HHmmss";
	private static final String SEP2 = "\\|";

	// TODO We can/should have better heuristics here
	// ie Check a few commits back just in case...
	public Collection<CCCommit> parse(String history) {
		return parse(history, new String[0], new String[0]);
	}

	public Collection<CCCommit> parse(String history, String[] branches,
			String[] includes) {
		Collection<CCCommit> commits = new TreeSet<CCCommit>(
				new CommitComparator());
		parse(history, commits);
		if (branches.length > 0) {
			filterBranches(commits, branches);
		}
		if (includes.length > 0 && !includes[0].equals(".")) {
			filterIncludes(commits, includes);
		}
		filter(commits);
		return commits;
	}

	private void parse(String history, Collection<CCCommit> commits) {
		for (String line : history.split(SEP)) {
			CCCommit commit = _parse(line);
			if (commit != null) {
				commits.add(commit);
			}
		}
	}

	private CCCommit _parse(String line) {
		String[] split = line.split(SEP2);
		String type = split[0];
		if (type.equals(DIR) || type.equals(FILE))
			return file(split);
		return null;
	}

	private CCCommit file(String[] split) {
		CCCommit commit = new CCCommit();
		try {
			commit.setDate(new SimpleDateFormat(DATE_FORMAT).parse(split[1]));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		commit.setAuthor(split[2]);
		CCFile file = new CCFile(split[3], split[4], getType(split[0]));
		commit.getFiles().add(file);
		commit.setMessage(split.length > 5 ? split[5] : "");
		return commit;
	}

	private Status getType(String string) {
		return DIR.equals(string) ? Status.Directory : Status.Added;
	}

	private void filter(Collection<CCCommit> commits) {
		CCCommit last = null;
		for (Iterator<CCCommit> i = commits.iterator(); i.hasNext();) {
			CCCommit next = i.next();
			CCFile newFile = next.getFiles().get(0);
			if (last != null && equals(last, next) && !contains(last, newFile)) {
				last.getFiles().add(newFile);
				last.setDate(next.getDate());
				i.remove();
			} else {
				last = next;
			}
		}
	}

	private boolean contains(CCCommit last, CCFile newFile) {
		for (CCFile file : last.getFiles()) {
			if (file.getFile().equals(newFile.getFile())) {
				return true;
			}
		}
		return false;
	}

	private void filterBranches(Collection<CCCommit> commits, String[] i) {
		Set<String> inc = new HashSet<String>(Arrays.asList(i));
		for (Iterator<CCCommit> j = commits.iterator(); j.hasNext();) {
			CCVersion version = j.next().getFiles().get(0).getVersion();
			if (!inc.contains(version.getBranch())) {
				j.remove();
			}
		}
	}

	private void filterIncludes(Collection<CCCommit> commits, String[] i) {
		for (Iterator<CCCommit> j = commits.iterator(); j.hasNext();) {
			CCCommit commit = j.next();
			for (Iterator<CCFile> k = commit.getFiles().iterator(); k.hasNext();) {
				String file = k.next().getFile();
				for (String f : i) {
					if (!file.equals(f) && !file.startsWith(f + "/")) {
						k.remove();
					}
				}
			}
			if (commit.getFiles().isEmpty()) {
				j.remove();
			}
		}
	}

	private boolean equals(CCCommit last, CCCommit next) {
		return getSubject(last.getMessage()).equals(
				getSubject(next.getMessage()))
				&& last.getAuthor().equals(next.getAuthor())
				&& last.getFiles().get(0).getVersion().getBranch().equals(
						next.getFiles().get(0).getVersion().getBranch());
	}

	private final class CommitComparator implements Comparator<CCCommit> {
		@Override
		public int compare(CCCommit o1, CCCommit o2) {
			int i = o1.getDate().compareTo(o2.getDate());
			return i != 0 ? i : -1;
		}
	}

	public List<String> getArgs(Date since, boolean ucm) {
		String format = LSH_FORMAT + getCommentFormat(ucm) + SEP;
		List<String> args = new ArrayList<String>();
		args.add("-r");
		args.add("-fmt");
		args.add(format);
		if (since != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(since);
			c.add(Calendar.SECOND, 1);
			since = c.getTime();
			args.add("-since");
			args.add(new SimpleDateFormat(CCHistoryParser.SINCE_DATE_FORMAT)
					.format(since));
		}
		return args;
	}

	private String getCommentFormat(boolean ucm) {
		return !ucm ? "%Nc" : "%[activity]Xp";
	}

}
