package gitcc;

import gitcc.cc.Clearcase;
import gitcc.cc.ClearcaseImpl;
import gitcc.cc.UCM;
import gitcc.cmd.Checkin;
import gitcc.cmd.Command;
import gitcc.cmd.Daemon;
import gitcc.cmd.Rebase;
import gitcc.cmd.Reset;
import gitcc.cmd.Update;
import gitcc.config.Config;
import gitcc.config.ConfigParser;
import gitcc.git.Git;
import gitcc.git.GitImpl;
import gitcc.util.ExecException;

import java.io.File;
import java.util.Arrays;

@SuppressWarnings("unchecked")
public class Gitcc {

	private static Class<Command>[] commands = new Class[] { Checkin.class,
			Rebase.class, Reset.class, Daemon.class, Update.class };

	public static void main(String[] args) throws Exception {
		ProxyHelper.initProxy();
		if (args.length == 0) {
			help();
		}
		String type = args[0];
		Command command = getCommand(type);

		Config config = command.config = new Config();
		Log.setLevel(config.getDebugLevel());
		Git git = command.git = new GitImpl(findGitRoot());
		command.init();
		config.setBranch(git.getBranch());

		ConfigParser parser = new ConfigParser();
		if (!parser.parseConfig(config, new File(git.getRoot(), ".git/gitcc")))
			fail("Missing configuration file: .git/gitcc");
		parser.parseConfig(config, new File(git.getRoot(), ".gitcc_config"));
		parser.loadUsers(config, new File(git.getRoot(), ".git/users"));

		command.cc = createClearcase(command.config);
		try {
			command.execute();
			System.exit(0);
		} catch (ExecException e) {
			fail(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void fail(String message) {
		System.err.println(message);
		System.exit(1);
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
