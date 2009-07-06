package gitcc.git;

public class GitCommit {

	private final String id;

	private final String author;

	private final String message;

	public GitCommit(String id, String author, String message) {
		this.id = id;
		this.author = author;
		this.message = message;
	}

	public String getId() {
		return id;
	}

	public String getAuthor() {
		return author;
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
