package gitcc.git;

public class FileStatus {

	public static enum Status {
		Added, Modified, Deleted, Renamed
	}

	private String file;
	private String oldFile;
	private Status status;

	public FileStatus(String file, Status status) {
		this(file, status, null);
	}

	public FileStatus(String file, Status status, String oldFile) {
		this.file = file;
		this.status = status;
		this.oldFile = oldFile;
	}

	public String getFile() {
		return file;
	}

	public String getOldFile() {
		return oldFile;
	}

	public Status getStatus() {
		return status;
	}
}
