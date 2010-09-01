package gitcc.cmd;

import gitcc.cc.CCCommit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class Import extends Rebase {

	public String backup = BACKUP;
	public String activities = ".git/activities.properties";

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

	@Override
	protected void loadActivities(Collection<CCCommit> commits) {
		try {
			Properties p = new Properties();
			if (new File(activities).exists()) {
				p.load(new FileReader(activities));
				for (CCCommit commit : commits) {
					String message = (String) p.get(commit.getMessage());
					if (message != null) {
						commit.setMessage(message);
					}
				}
			} else {
				List<String> ids = new ArrayList<String>(commits.size());
				for (CCCommit commit : commits) {
					ids.add(commit.getMessage());
				}
				super.loadActivities(commits);
				int i = 0;
				for (CCCommit commit : commits) {
					p.put(ids.get(i), commit.getMessage());
					i++;
				}
				p.store(new FileWriter(activities), null);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
