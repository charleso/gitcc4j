package gitcc;

import gitcc.cc.Clearcase;
import gitcc.cc.ClearcaseImpl;
import gitcc.cc.UCM;
import gitcc.cmd.Checkin;
import gitcc.cmd.Command;
import gitcc.cmd.Rebase;
import gitcc.cmd.Reset;
import gitcc.config.Config;
import gitcc.config.ConfigParser;
import gitcc.config.ConfigValidator;
import gitcc.git.Git;
import gitcc.git.GitImpl;
import gitcc.util.ExecException;

import java.io.File;
import java.util.Arrays;

@SuppressWarnings("unchecked")
public class Gitcc {

	private static Class<Command>[] commands = new Class[] { Checkin.class,
			Rebase.class, Reset.class };

	public static void main(String[] args) throws Exception {
		ProxyHelper.initProxy();
		if (args.length == 0) {
			help();
		}
		String type = args[0];
		Command command = getCommand(type);

		Config config = command.config = new Config();
		Log.setDebug(config.isDebug());
		Git git = command.git = new GitImpl(findGitRoot(), config);
		config.setBranch(git.getBranch());
		try {
			new ConfigParser().parseConfig(config, git.getRoot());
		} catch (Exception e) {
			throw new RuntimeException("Missing configuation file", e);
		}
		try {
			new ConfigValidator().validate(config);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		command.cc = createClearcase(command.config);

		try {
			command.execute();
			System.exit(0);
		} catch (ExecException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static File findGitRoot() {
		File dir = new File(".").getAbsoluteFile();
		while (!Arrays.asList(dir.list()).contains(".git")) {
			dir = dir.getParentFile();
			if (dir == null)
				throw new RuntimeException("No git directory found");
		}
		return dir;
	}

	private static Clearcase createClearcase(Config config) throws Exception {
		if (config.isUCM()) {
			return new UCM(config);
		} else {
			return new ClearcaseImpl(config);
		}
	}

	private static Command getCommand(String type) throws Exception {
		for (Class<Command> c : commands) {
			if (c.getSimpleName().equalsIgnoreCase(type)) {
				return c.newInstance();
			}
		}
		help();
		return null;
	}

	private static void help() throws Exception {
		System.out.println("gitcc COMMAND [ARGS]");
		System.exit(1);
	}
}
