package gitcc.git;

public class GitCommit {

	private String id;

	private String message;

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public String getSubject() {
		return getSubject(message);
	}

	public static String getSubject(String message) {
		return message.split("\n")[0];
	}
}
