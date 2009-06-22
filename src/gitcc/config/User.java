package gitcc.config;

public class User {

	private final String username;
	private final String name;
	private final String password;
	private final String email;

	public User(String username, String name, String email, String password) {
		super();
		this.username = username;
		this.name = name;
		this.password = password;
		this.email = email;
	}

	public User(String username, String suffix) {
		this(username, username, username + "@" + suffix, null);
	}

	public String getUsername() {
		return username;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}
}
