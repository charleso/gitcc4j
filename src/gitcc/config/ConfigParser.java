package gitcc.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ConfigParser {

	private static final String SEP = "\\|";

	public void parseConfig(Config config, File root) throws Exception {
		File file = new File(root, ".git/gitcc");
		BufferedReader in = new BufferedReader(new FileReader(file));
		String mode = "core";
		for (String line; (line = in.readLine()) != null;) {
			line = line.trim();
			if (line.length() == 0)
				continue;
			if (line.startsWith("[")) {
				mode = line.substring(1, line.length() - 1);
			} else {
				String[] values = line.split("=", 2);
				parseValue(config, mode, values[0].trim(), values[1].trim());
			}
		}
	}

	private void parseValue(Config config, String mode, String name,
			String value) {
		if ("core".equals(mode)) {
			if ("debug".equals(name))
				config.setDebug(Boolean.parseBoolean(value));
			else if ("include".equals(name))
				config.setInclude(value.split(SEP));
			else if ("type".equals(name))
				config.setType(value);
			else if ("url".equals(name))
				config.setUrl(value);
			else if ("username".equals(name))
				config.setUsername(value);
			else if ("password".equals(name))
				config.setPassword(value);
			else if ("suffix".equals(name))
				config.setSuffix(value);
			else if ("group".equals(name))
				config.setGroup(value);
		} else {
			if (mode.equals(config._getBranch())) {
				if ("clearcase".equals(name))
					config.setClearcase(value);
				else if ("branches".equals(name))
					config.setBranches(value.split(SEP));
			}
		}
	}
}