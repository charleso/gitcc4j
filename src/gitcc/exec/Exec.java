package gitcc.exec;

import gitcc.Log;
import gitcc.util.ExecException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Exec {

	private final String[] cmd;
	private File root;

	public Exec(String... cmd) {
		this.cmd = cmd;
	}

	public void setRoot(File root) {
		this.root = root;
	}

	public byte[] _exec(String... args) {
		return _exec(null, args);
	}

	private String[] preExec(String... args) {
		String[] _args = new String[args.length + cmd.length];
		System.arraycopy(args, 0, _args, cmd.length, args.length);
		for (int i = 0; i < cmd.length; i++) {
			_args[i] = cmd[i];
		}
		debug(_args);
		return _args;
	}

	protected Process startProcess(String[] env, String... args) {
		try {
			return Runtime.getRuntime().exec(preExec(args), env, root);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] _exec(String[] env, String... args) {
		try {
			Process process = startProcess(env, args);
			byte[] stdout = getBytes(process.getInputStream());
			String error = new String(getBytes(process.getErrorStream()));
			if (process.waitFor() > 0) {
				throw new ExecException(error + bytesToString(stdout));
			}
			return stdout;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private byte[] getBytes(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		for (int i = 0; (i = in.read(b)) != -1;) {
			out.write(b, 0, i);
		}
		return out.toByteArray();
	}

	private void debug(String[] _args) {
		StringBuilder b = new StringBuilder();
		for (String arg : _args) {
			b.append(arg).append(" ");
		}
		Log.debug(b.toString());
	}

	public String exec(String[] env, String... args) {
		return bytesToString(_exec(env, args));
	}

	public String exec(String... args) {
		return exec(null, args);
	}

	public File getRoot() {
		return root;
	}

	private String bytesToString(byte[] stdout) {
		return new String(stdout).trim();
	}

}
