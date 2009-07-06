package gitcc.cc;

import gitcc.config.User;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface Clearcase {

	void update();

	void rebase();

	void uncheckout(String f);

	void checkout(String file);

	void checkin(String file, String message);

	void deliver();

	String mkact(String message);

	void rmact(String activity);

	void move(String file, String newFile);

	void delete(String file);

	void add(String file, String message);

	void mkdir(String dir);

	Collection<CCCommit> getHistory(Date since);

	List<CCFile> diffPred(CCFile file);

	File get(CCFile f);

	boolean exists(String path);

	void write(String file, byte[] bytes);

	File toFile(String file);

	Clearcase cloneForUser(User user) throws Exception;

	void makeBaseline();
}