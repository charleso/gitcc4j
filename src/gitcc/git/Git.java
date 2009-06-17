package gitcc.git;

import gitcc.cc.CCCommit;

import java.io.File;
import java.util.Date;
import java.util.List;

public interface Git {

	void add(String file);

	void remove(String file);

	void commit(CCCommit commit);

	String getBranch();

	void rebase(String upstream, String branch);

	List<GitCommit> log(String treeish);

	List<FileStatus> getStatuses(GitCommit c);

	byte[] catFile(String sha, String file);

	File getRoot();

	Date getCommitDate(String cc);

	void branch(String branch);

	void tag(String tag, String id);

	void checkout(String branch);

}