package gitcc.cc;

import gitcc.cc.CCFile.Status;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DirDiffUtil {

	private final boolean delete;

	public DirDiffUtil(boolean delete) {
		this.delete = delete;
	}

	public List<CCFile> diff(String root, File a, File b) {
		Set<String> d1 = getFiles(a);
		Set<String> d2 = getFiles(b);
		List<CCFile> files = new ArrayList<CCFile>();
		Set<String> d3 = new HashSet<String>(d2);
		d3.removeAll(d1);
		for (String s : d3) {
			files.add(new CCFile(rel(root, s), "", Status.Added));
		}
		d1.removeAll(d2);
		for (String s : d1) {
			files.add(new CCFile(rel(root, s), "", Status.Deleted));
		}
		return files;
	}

	private String rel(String root, String s) {
		return root + "/" + s;
	}

	private Set<String> getFiles(File f) {
		try {
			return _getFiles(f);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Set<String> _getFiles(File f) throws Exception {
		Set<String> files = new HashSet<String>();
		BufferedReader r = new BufferedReader(new FileReader(f));
		r.readLine();
		r.readLine();
		for (String line; (line = r.readLine()) != null;) {
			String[] s = line.split(":");
			files.add(s[1].substring(0, Integer.parseInt(s[0])));
		}
		r.close();
		if (delete) {
			f.delete();
		}
		return files;
	}
}
