package gitcc.exec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Args {

	private final List<String> args;

	public Args(String... args) {
		this.args = new ArrayList<String>(Arrays.asList(args));
	}

	public Args add(String arg) {
		args.add(arg);
		return this;
	}

	public String[] toArray() {
		return args.toArray(new String[args.size()]);
	}
}
