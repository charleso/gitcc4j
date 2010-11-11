package gitcc;

import gitcc.cc.Clearcase;
import gitcc.cmd.Checkin;
import gitcc.cmd.Command;
import gitcc.cmd.Daemon;
import gitcc.cmd.Import;
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
			Rebase.class, Reset.class, Daemon.class, Update.class, Import.class };

	public static void main(String[] args) throws Exception {
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
		command.cc.setConfig(command.config);
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

	public static Clearcase createClearcase(Config config) throws Exception {
		/*
		 * We try these implementations in this order.  For each implementation
		 * we first load the class.  If the class is found then we call a method
		 * in it that checks if the necessary tools are available on the machine.
		 * If not then we go on to the next implementation.  
		 * 
		 * It may be that the class is not found.  This could happen if
		 * the implementation is in another jar and that jar was not included
		 * in the build.  For example a user may not have access to the CCRC libraries,
		 * in which case the user cannot build the ClearGit.ccrc jar.
		 */
		String [] factoryClassNames = new String [] {
//				"gitcc.cc.cleartool.CleartoolFactory",
				"gitcc.ccrc.CcrcFactory"
		};

		StringBuffer errorMessage = new StringBuffer();
		errorMessage.append("Unable to access ClearCase for the following reasons:\n");
		
		for (String factoryClassName : factoryClassNames) {
			try {
				ClearcaseFactory factory = (ClearcaseFactory) Class.forName(factoryClassName).newInstance();
				return factory.createClearcase(config);
			} catch (ClearcaseEnvironmentNotSetup e) {
				errorMessage.append(factoryClassName + " cannot be used because " + e.getMessage() + ".\n");
			} catch (ClassNotFoundException e) {
				errorMessage.append(factoryClassName + " cannot be used because the class is not included in this build of Gitcc4j.\n");
			}
		}
		
		fail(errorMessage.toString());
		return null;
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
		for (Class<Command> c : commands) {
			System.out.println("gitcc " + c.getSimpleName().toLowerCase() + " [ARGS]");
		}
		
		System.exit(1);
	}
}
