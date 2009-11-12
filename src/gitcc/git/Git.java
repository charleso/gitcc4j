package gitcc.git;

import gitcc.cc.CCCommit;
import gitcc.config.User;

import java.io.File;
import java.util.Date;
import java.util.List;

public interface Git {

	String HEAD = "HEAD";

	void add(String file);

	void addForce(String file);

	void remove(String file);

	void commit(CCCommit commit, User user);

	String getBranch();

	void rebase(String upstream, String branch);

	List<GitCommit> log(String treeish);

	List<FileStatus> getStatuses(GitCommit c);

	byte[] catFile(String sha, String file);

	File getRoot();

	Date getCommitDate(String cc);

	void branch(String branch);

	void branchForce(String branch, String id);

	void checkout(String branch);

	void checkoutForce(String branch);

	String mergeBase(String commit1, String commit2);

	String hashObject(String file);

	String getBlob(String file, String mergeBase);

	String getId(String treeish);

	void setConfig(String name, String value);

	String getConfig(String name);

	void merge(String remote);

	void gc();

	String diffTree(String commit1, String commit2);

	void resetHard(String treeish);

	void pullRebase(String remote);

	void push(String remote);
}