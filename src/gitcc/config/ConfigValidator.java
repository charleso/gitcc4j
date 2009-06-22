package gitcc.config;

public class ConfigValidator {

	public void validate(Config config) {
		if (config.getClearcase() == null) {
			throw new RuntimeException(
					"Missing required configuration for 'clearcase'. "
							+ "Are you on the right branch?");
		}
	}
}
