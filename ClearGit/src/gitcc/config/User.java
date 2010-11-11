package gitcc.config;

public class User {

	private final String username;
	private final String name;
	private final String password;
	private final String email;
	private String view;

	public User(String username, String name, String email, String password) {
		super();
		this.username = username;
		this.name = name;
		this.password = password;
		this.email = email;
	}

	public User(String email) {
		this(email.substring(0, email.indexOf('@')), email, email, null);
	}

	public void setView(String view) {
		this.view = view;
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

	public String getView() {
		return view;
	}
}
