package gitcc.cc;

import gitcc.Log;
import gitcc.config.Config;
import gitcc.config.User;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.ibm.rational.clearcase.remote_core.cmds.Checkin;
import com.ibm.rational.clearcase.remote_core.cmds.Checkout;
import com.ibm.rational.clearcase.remote_core.cmds.Cleartool;
import com.ibm.rational.clearcase.remote_core.cmds.CmdProgress;
import com.ibm.rational.clearcase.remote_core.cmds.Compare;
import com.ibm.rational.clearcase.remote_core.cmds.IVectoredFileCmdListener;
import com.ibm.rational.clearcase.remote_core.cmds.MaterializedVersion;
import com.ibm.rational.clearcase.remote_core.cmds.Mkelem;
import com.ibm.rational.clearcase.remote_core.cmds.Mv;
import com.ibm.rational.clearcase.remote_core.cmds.Rmname;
import com.ibm.rational.clearcase.remote_core.cmds.Uncheckout;
import com.ibm.rational.clearcase.remote_core.cmds.Version;
import com.ibm.rational.clearcase.remote_core.cmds.Checkout.Item;
import com.ibm.rational.clearcase.remote_core.cmds.Checkout.NonLatestTreatment;
import com.ibm.rational.clearcase.remote_core.cmds.sync.Update;
import com.ibm.rational.clearcase.remote_core.cmds.sync.UpdateListenerAdapter;
import com.ibm.rational.clearcase.remote_core.copyarea.CopyArea;
import com.ibm.rational.clearcase.remote_core.copyarea.CopyAreaFile;
import com.ibm.rational.clearcase.remote_core.copyarea.HijackTreatment;
import com.ibm.rational.clearcase.remote_core.rpc.Session;
import com.ibm.rational.clearcase.remote_core.util.Status;

public class ClearcaseImpl extends BaseClearcase implements Clearcase {

	private static final String LSH_FORMAT = "%o%m|%Nd|%u|%En|%Vn|";
	protected static final HijackTreatment HIJACK_TREATMENT = HijackTreatment.OVERWRITE;
	private static final String DATE_FORMAT = "dd-MMM-yyyy.HH:mm:ss";
	private static final String RPC_PATH = "/TeamCcrc/ccrc/";

	protected Config config;
	protected Session session;

	protected CopyArea copyArea;
	private CopyArea root;
	private CopyAreaFile[] files;
	private String extraPath;

	public ClearcaseImpl(Config config) {
		this.config = config;
		try {
			String url = new URL(new URL(config.getUrl()), RPC_PATH).toString();
			session = new Session(url, new Credentials(config));
			setRoot(config.getClearcase());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected ClearcaseImpl() {
		super();
	}

	private void setRoot(String configPath) throws Exception {
		root = new CopyAreaFile(new File(configPath)).getCopyArea();
		extraPath = configPath.substring(root.getRoot().length() + 1)
				+ File.separatorChar;
		String[] includes = config.getInclude();
		files = new CopyAreaFile[includes.length];
		for (int i = 0; i < includes.length; i++) {
			files[i] = new CopyAreaFile(new File(new File(configPath),
					includes[i]));
		}
		copyArea = files[0].getCopyArea();
	}

	private CopyAreaFile[] singleFile(String file) {
		return new CopyAreaFile[] { copyFile(file) };
	}

	private CopyAreaFile copyFile(String file) {
		return new CopyAreaFile(root, extraPath + file);
	}

	public File toFile(String file) {
		return copyFile(file);
	}

	@Override
	public void update() {
		debug("update");
		run(new Update(session, new UpdateListener(), files, HIJACK_TREATMENT,
				false, false));
	}

	@Override
	public void rebase() {
		// Do nothing
	}

	@Override
	public void checkout(String file) {
		debug("checkout " + file);
		run(new Checkout(session, log(Checkout.Listener.class), null, false,
				(String) null, false, false, NonLatestTreatment.FAIL,
				singleFile(file)));
	}

	@Override
	public void uncheckout(String file) {
		debug("unco " + file);
		run(new Uncheckout(session, log(IVectoredFileCmdListener.class), false,
				singleFile(file)));
	}

	@Override
	public void write(String file, byte[] bytes) {
		try {
			FileOutputStream writer = new FileOutputStream(copyFile(file));
			writer.write(bytes);
			writer.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void checkin(String file, String message) {
		debug("checkin -c \"" + message + "\" " + file);
		Item item = new Item(copyFile(file), message, (String) null);
		run(new Checkin(session, log(Checkin.Listener.class), true,
				new Item[] { item }));
	}

	@Override
	public void deliver() {
		// Do nothing
	}

	@Override
	public void makeBaseline() {
		// Ignore
	}

	@Override
	public String mkact(String message) {
		return message;
	}

	@Override
	public void rmact(String activity) {
		// Ignore
	}

	@Override
	public void add(String file, String comment) {
		debug("mkelem " + file);
		mkelem(file, null, comment);
	}

	@Override
	public void mkdir(String dir) {
		debug("mkelem -eltype directory " + dir);
		copyFile(dir).mkdirs();
		mkelem(dir, "directory", null);
	}

	private void mkelem(String file, String type, String comment) {
		run(new Mkelem(session, log(IVectoredFileCmdListener.class), comment,
				(String) null, type == null, type, singleFile(file)));
	}

	@Override
	public void delete(String file) {
		debug("rm " + file);
		run(new Rmname(session, log(CmdProgress.UI.class), null,
				singleFile(file)));
	}

	@Override
	public void move(String file, String newFile) {
		debug(String.format("mv %s %s", file, newFile));
		run(new Mv(session, log(CmdProgress.UI.class), null, copyFile(file),
				copyFile(newFile)));
	}

	@Override
	public String getHistory(Date since) {
		cd();
		String format = LSH_FORMAT + getCommentFormat() + CCHistoryParser.SEP;
		List<String> args = new ArrayList<String>();
		args.add("-r");
		args.add("-fmt");
		args.add(format);
		if (since != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(since);
			c.add(Calendar.SECOND, 1);
			since = c.getTime();
			args.add("-since");
			args.add(new SimpleDateFormat(DATE_FORMAT).format(since));
		}
		for (CopyAreaFile include : files) {
			args.add(include.getCopyAreaRelPname());
		}
		String[] _args = (String[]) args.toArray(new String[args.size()]);
		return cleartool("lshistory", _args);
	}

	@Override
	public void fixFile(CCFile f) {
		if (f.getFile().startsWith(extraPath)) {
			f.setFile(f.getFile().substring(extraPath.length()));
		}
	}

	// TODO This is just wrong. We need to find another
	// way to get the history. :(
	private void cd() {
		for (String line : cleartool("lsview").split("\n")) {
			String[] s = line.trim().split(" ");
			if (root.getRoot().endsWith(s[0])) {
				String path = s[s.length - 1];
				path = path.substring(path.indexOf('/'));
				path = path.replaceAll(".view.stg", "");
				cleartool("cd", path);
				return;
			}
		}
		throw new RuntimeException("View not found");
	}

	protected String getCommentFormat() {
		return "%Nc";
	}

	@Override
	public String getRealComment(String comment) {
		return comment;
	}

	protected String cleartool(String cmd, String... args) {
		debug(cmd + " " + Arrays.asList(args));
		final StringBuilder b = new StringBuilder();
		run(new Cleartool(session, new Cleartool.Listener() {
			@Override
			public void nextLine(String line) {
				b.append(line).append("\n");
			}
		}, cmd, args));
		return b.toString();
	}

	@Override
	public List<CCFile> diffPred(CCFile file) {
		debug("diff -pred " + file);
		Version v = versionOf(file);
		CopyAreaFile[] files = new CopyAreaFile[2];
		run(new Compare(session, new Version[] { v.prev(), v },
				new CompareListener(files)));
		return new DirDiffUtil(true).diff(file.getFile(), files[0], files[1]);
	}

	@Override
	public File get(CCFile f) {
		debug("get -to " + f.getFile() + " " + f);
		CopyAreaFile[] files = new CopyAreaFile[1];
		run(new Compare(session, new Version[] { versionOf(f) },
				new CompareListener(files)));
		return files[0];
	}

	private Version versionOf(CCFile file) {
		return new Version(copyFile(file.getFile()), file.getVersion()
				.getFullVersion());
	}

	@Override
	public boolean exists(String path) {
		return copyFile(path).exists();
	}

	@Override
	public Clearcase cloneForUser(User user) throws Exception {
		if (user == null || user.getPassword() == null)
			return this;
		Credentials credentials = new Credentials(user.getUsername(), user
				.getPassword(), config.getGroup());
		ClearcaseImpl cc = getClass().newInstance();
		cc.config = config;
		cc.session = new Session(session.getUrl(), credentials);
		String view = user.getView();
		cc.setRoot(view != null ? view : config.getClearcase());
		return cc;
	}

	protected void debug(String s) {
		Log.debug("cleartool " + s);
	}

	protected final class UpdateListener extends UpdateListenerAdapter {
		@Override
		public void errorOccurred(Status status) {
			throw new RuntimeException(status.toString());
		}
	}

	private final class CompareListener implements Compare.Listener {

		private final CopyAreaFile[] files;

		private CompareListener(CopyAreaFile[] files) {
			this.files = files;
		}

		@Override
		public void endContributor(String path, MaterializedVersion version) {
			int i = files[0] == null ? 0 : 1;
			files[i] = (CopyAreaFile) version.getFile();
		}

		@Override
		public void runComplete(Status status) {
			if (!status.isOk())
				throw new RuntimeException(status.getMsg());
		}

		@Override
		public void beginContributor(String arg0, MaterializedVersion arg1) {
			// Ignore
		}

		@Override
		public void xferProgress(File file, long l, long l1) {
			// Ignore
		}
	}
}
