package gitcc.git;

import gitcc.cc.CCCommit;
import gitcc.config.User;
import gitcc.util.ExecException;
import gitcc.util.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DefaultImport implements Import {

	private Git git;

	public DefaultImport(Git git) {
		this.git = git;
	}

	@Override
	public void checkout(String branch) {
		git.checkout(branch);
	}

	@Override
	public void add(String path, long length, InputStream in) {
		File dest = new File(git.getRoot(), path);
		dest.getParentFile().mkdirs();
		if (dest.exists() && !dest.delete())
			throw new RuntimeException("Could not delete file: " + dest);
		try {
			OutputStream out = new FileOutputStream(new File(path));
			IOUtils.copy(in, new BufferedOutputStream(out));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		try {
			git.add(path);
		} catch (ExecException e) {
			if (e.getMessage().contains("The following paths are ignored"))
				return;
			throw e;
		}
	}

	@Override
	public void commit(CCCommit commit, User user) {
		git.commit(commit, user);
	}

	@Override
	public void remove(String path) {
		git.remove(path);
	}

	@Override
	public void close() {
		// Do nothing
	}
}
