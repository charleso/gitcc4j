package gitcc.git;

import gitcc.cc.CCCommit;
import gitcc.config.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FastImport implements Import {

	private final List<ImportFile> files = new ArrayList<ImportFile>();
	private final OutputStream writer;
	private final byte[] buffer = new byte[1024];

	private String branch;
	private int mark;

	public FastImport(OutputStream writer) {
		this.writer = writer;
	}

	public void checkout(String branch) {
		this.branch = "refs/heads/" + branch;
	}

	public void add(String path, long length, InputStream in) {
		print("blob");
		int mark = mark();
		data(length);
		write(in);
		print();
		print();
		files.add(new ImportFile(path, mark));
	}

	public void remove(String path) {
		files.add(new ImportFile(path));
	}

	public void commit(CCCommit commit, User user) {
		print("commit %s", branch);
		mark();
		long time = commit.getDate().getTime();
		print("author %s <%s> %s", user.getName(), user.getEmail(), time);
		print("committer %s <%s> %s", user.getName(), user.getEmail(), time);
		data(commit.getMessage().length());
		print(commit.getMessage());
		for (ImportFile file : files) {
			if (file.isAdded()) {
				print("M 100644 :%s %s", file.mark, file.path);
			} else {
				print("D %s", file.path);
			}
		}
		files.clear();
		print();
	}

	@Override
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private int mark() {
		print("mark :" + ++mark);
		return mark;
	}

	private void data(long size) {
		print("data %s", size);
	}

	private void print() {
		print("");
	}

	private void print(String string, Object... args) {
		try {
			writer.write(String.format(string, args).getBytes("ascii"));
			writer.write(10);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void write(InputStream in) {
		try {
			for (int i = 0; (i = in.read(buffer)) > -1;) {
				writer.write(buffer, 0, i);
			}
			in.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static class ImportFile {

		private final int mark;
		private final String path;

		public ImportFile(String path) {
			this(path, 0);
		}

		public ImportFile(String path, int mark) {
			this.path = path;
			this.mark = mark;
		}

		public boolean isAdded() {
			return mark > 0;
		}
	}
}
