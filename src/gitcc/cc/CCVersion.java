package gitcc.cc;

public class CCVersion {

	private final String version;

	public CCVersion(String version) {
		this.version = version;
	}

	public String getBranch() {
		String[] s = version.split("/");
		return s[s.length - 2];
	}

	public String getFullVersion() {
		return version;
	}

	@Override
	public boolean equals(Object obj) {
		return ((CCVersion) obj).version.equals(version);
	}
}
