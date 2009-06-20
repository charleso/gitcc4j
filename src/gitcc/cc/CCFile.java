package gitcc.cc;

public class CCFile {

	public static enum Status {
		Added, Deleted, Directory
	}

	private String file;
	private final CCVersion version;
	private final Status status;

	public CCFile(String file, String version, Status status) {
		this.file = file;
		this.version = new CCVersion(version);
		this.status = status;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public CCVersion getVersion() {
		return version;
	}

	public Status getStatus() {
		return status;
	}

	public boolean hasVersion() {
		return version.getFullVersion().length() > 0;
	}

	@Override
	public boolean equals(Object obj) {
		CCFile f = (CCFile) obj;
		return f.file.equals(file) && f.version.equals(version);
	}

	@Override
	public String toString() {
		return file + "@@" + version.getFullVersion();
	}
}
