package gitcc.git;

import gitcc.cc.CCCommit;
import gitcc.config.User;

import java.io.InputStream;

public interface Import {

	void checkout(String branch);

	void add(String path, long length, InputStream in);

	void remove(String path);

	void commit(CCCommit commit, User user);

	void close();
}
