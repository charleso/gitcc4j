package gitcc.cc;

import gitcc.Log;
import gitcc.config.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
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
	private static final CCHistoryParser histParser = new CCHistoryParser();
	private static final String DATE_FORMAT = "dd-MMM-yyyy.HH:mm:ss";
	private static final String RPC_PATH = "/TeamCcrc/ccrc/";

	protected final Session session;
	protected final CopyArea copyArea;

	private final CopyArea root;
	private final CopyAreaFile[] files;
	private final String[] branches;
	private final String extraPath;

	public ClearcaseImpl(Config config) {
		try {
			String url = new URL(new URL(config.getUrl()), RPC_PATH).toString();
			session = new Session(url, new Credentials(config));
			String configPath = config.getClearcase();
			root = new CopyAreaFile(new File(configPath)).getCopyArea();
			extraPath = configPath.substring(root.getRoot().length() + 1)
					+ File.separatorChar;
			files = convert(config);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		copyArea = files[0].getCopyArea();
		branches = config.getBranches();
	}

	private ClearcaseImpl(ClearcaseImpl cc, Session session) {
		this.session = cc.session;
		this.copyArea = cc.copyArea;
		this.root = cc.root;
		this.files = cc.files;
		this.branches = cc.branches;
		this.extraPath = cc.extraPath;
	}

	private CopyAreaFile[] convert(Config config) throws Exception {
		String[] includes = config.getInclude();
		CopyAreaFile[] files = new CopyAreaFile[includes.length];
		String _root = config.getClearcase();
		for (int i = 0; i < includes.length; i++) {
			files[i] = new CopyAreaFile(new File(new File(_root), includes[i]));
		}
		return files;
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
		Item item = new Item(copyFile(file), message, (String) null);
		run(new Checkin(session, log(Checkin.Listener.class), true,
				new Item[] { item }));
	}

	@Override
	public void deliver() {
		// Do nothing
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
	public Collection<CCCommit> getHistory(Date since) {
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
		String lsh = cleartool("lshistory", _args);
		Collection<CCCommit> commits = histParser.parse(lsh, branches);
		for (CCCommit commit : commits) {
			for (CCFile f : commit.getFiles()) {
				f.setFile(f.getFile().substring(extraPath.length()));
			}
			commit.setMessage(getRealComment(commit.getMessage()));
		}
		return commits;
	}

	// TODO This is just wrong. We need to find another
	// way to get the history. :(
	private void cd() {
		for (String line : cleartool("lsview").split("\n")) {
			String[] s = line.trim().split(" ");
			if (root.getRoot().contains(s[0])) {
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

	protected String getRealComment(String comment) {
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
	public Clearcase getSession(Credentials credentials) {
		if (credentials.getPassword() == null)
			return this;
		Session session2 = new Session(session.getUrl(), credentials);
		return new ClearcaseImpl(this, session2);
	}

	private void debug(String s) {
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
