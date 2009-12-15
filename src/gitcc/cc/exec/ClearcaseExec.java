package gitcc.cc.exec;

import gitcc.cc.CCFile;
import gitcc.cc.CCHistoryParser;
import gitcc.cc.Clearcase;
import gitcc.cc.CCFile.Status;
import gitcc.config.Config;
import gitcc.config.User;
import gitcc.exec.Exec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClearcaseExec extends Exec implements Clearcase {

	private static final CCHistoryParser histParser = new CCHistoryParser();

	private Config config;
	private String password;

	@Override
	public void setConfig(Config config) {
		this.config = config;
		setCmd(config.getCleartool());
		setRoot(new File(config.getClearcase()));
	}

	@Override
	public Clearcase cloneForUser(User user) throws Exception {
		ClearcaseExec cc = getClass().newInstance();
		cc.setCmd("su", user.getUsername(), "-c", config.getCleartool());
		cc.password = user.getPassword();
		// TODO Calculate view + create if required
		cc.setRoot(new File(user.getView()));
		return cc;
	}

	@Override
	protected Process startProcess(String[] env, String... args)
			throws IOException {
		Process process = super.startProcess(env, args);
		if (password != null) {
			process.getOutputStream().write(password.getBytes());
		}
		return process;
	}

	@Override
	public List<CCFile> diffPred(CCFile file) {
		String diff = exec("diff", "-diff_format", "-pred", file.toString());
		return diff(file.getFile(), diff);
	}

	protected List<CCFile> diff(String dir, String diff) {
		List<CCFile> files = new ArrayList<CCFile>();
		String sep = File.separator;
		for (String line : diff.split("\n")) {
			if (line.contains(" -> "))
				continue;
			int i = Math.max(line.indexOf("  "), line.indexOf(sep + " "));
			if (i < 0)
				continue;
			String file = dir + sep + line.substring(2, i);
			if (line.startsWith("<")) {
				files.add(new CCFile(file, "", Status.Deleted));
			} else if (line.startsWith(">")) {
				files.add(new CCFile(file, "", Status.Added));
			}
		}
		return files;
	}

	@Override
	public String getHistory(Date since) {
		List<String> args = histParser.getArgs(since, this instanceof UCMExec);
		for (String include : config.getInclude()) {
			args.add(include);
		}
		args.add(0, "lshistory");
		String[] _args = (String[]) args.toArray(new String[args.size()]);
		return exec(_args);
	}

	@Override
	public void add(String file, String message) {
		exec("mkelem", "-c", message, file);
	}

	@Override
	public void checkin(String file, String message) {
		exec("ci", "-identical", "-c", message, file);
	}

	@Override
	public void checkout(String file) {
		exec("co", "-reserved", "-nc", file);
	}

	@Override
	public void delete(String file) {
		exec("rm", file);
	}

	@Override
	public void deliver() {
		exec("deliver", "-f");
		exec("deliver", "-com", "-f");
	}

	@Override
	public void mkdir(String dir) {
		exec("mkelem", "-eltype", "directory", dir);
	}

	@Override
	public void move(String file, String newFile) {
		exec("mv", "-nc", file, newFile);
	}

	@Override
	public void uncheckout(String f) {
		exec("unco", "-rm", f);
	}

	@Override
	public void update() {
		exec("update", ".");
	}

	// UCM methods

	@Override
	public String mkact(String message) {
		return null;
	}

	@Override
	public void makeBaseline() {
		// Ignore
	}

	@Override
	public void rebase() {
		// Ignore
	}

	@Override
	public void rmact(String activity) {
		// Ignore
	}

	@Override
	public File get(CCFile f) {
		try {
			File temp = File.createTempFile("gitcc", ".tmp");
			temp.delete();
			exec("get", "-to", temp.getAbsolutePath(), f.toString());
			temp.setWritable(true);
			return temp;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void sync() {
		// Ignore
	}

	@Override
	public void fixFile(CCFile f) {
		// TODO Not sure?
	}

	@Override
	public String getRealComment(String comment) {
		return comment;
	}

	// TODO Not implementation specific

	@Override
	public File toFile(String file) {
		return new File(getRoot(), file);
	}

	@Override
	public boolean exists(String path) {
		return toFile(path).exists();
	}

	public static boolean isAvailable(Config config) {
		try {
			Runtime.getRuntime().exec(config.getCleartool()).destroy();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
