package gitcc.cc;

import static gitcc.git.GitCommit.getSubject;
import gitcc.cc.CCFile.Status;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class CCHistoryParser {

	private static final String FILE = "checkinversion";
	private static final String DIR = "checkindirectory version";
	public static final String SEP = "@@@";
	private static final String DATE_FORMAT = "yyyyMMdd.HHmmss";
	private static final String SEP2 = "\\|";
	private static final String EMPTY = "<empty message>";

	// TODO We can/should have better heuristics here
	// ie Check a few commits back just in case...
	public Collection<CCCommit> parse(String history, String[] includes) {
		Collection<CCCommit> commits = new TreeSet<CCCommit>(
				new CommitComparator());
		parse(history, commits, includes);
		filter(commits);
		return commits;
	}

	private void parse(String history, Collection<CCCommit> commits, String[] i) {
		Set<String> includes = new HashSet<String>(Arrays.asList(i));
		for (String line : history.split(SEP)) {
			CCCommit commit = _parse(line);
			if (commit != null) {
				CCVersion version = commit.getFiles().get(0).getVersion();
				if (includes.contains(version.getBranch())) {
					commits.add(commit);
				}
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
		commit.setMessage(split.length > 5 ? split[5] : EMPTY);
		return commit;
	}

	private Status getType(String string) {
		return DIR.equals(string) ? Status.Directory : Status.Added;
	}

	private void filter(Collection<CCCommit> commits) {
		CCCommit last = null;
		for (Iterator<CCCommit> i = commits.iterator(); i.hasNext();) {
			CCCommit next = i.next();
			if (last != null && equals(last, next)) {
				last.getFiles().add(next.getFiles().get(0));
				last.setDate(next.getDate());
				i.remove();
			} else {
				last = next;
			}
		}
	}

	private boolean equals(CCCommit last, CCCommit next) {
		return getSubject(last.getMessage()).equals(
				getSubject(next.getMessage()))
				&& last.getAuthor().equals(next.getAuthor());
	}

	private final class CommitComparator implements Comparator<CCCommit> {
		@Override
		public int compare(CCCommit o1, CCCommit o2) {
			int i = o1.getDate().compareTo(o2.getDate());
			return i != 0 ? i : -1;
		}
	}
}
