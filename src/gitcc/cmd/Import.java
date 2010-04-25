package gitcc.cmd;

import gitcc.cc.CCCommit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class Import extends Rebase {

	public String backup = BACKUP;

	@Override
	protected Collection<CCCommit> getHistory(Date since) {
		String lsh = loadHistory();
		Collection<CCCommit> history = parseHistory(lsh);
		for (Iterator<CCCommit> i = history.iterator(); i.hasNext();) {
			CCCommit commit = i.next();
			if (since != null && commit.getDate().before(since)) {
				i.remove();
				System.out.println("Skipping: " + commit.getMessage());
			}
		}
		return history;
	}

	private String loadHistory() {
		try {
			return _loadHistory();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String _loadHistory() throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(backup));
		StringBuilder b = new StringBuilder();
		for (String line; (line = reader.readLine()) != null;) {
			b.append(line).append("\n");
		}
		String lsh = b.toString();
		return lsh;
	}
}
