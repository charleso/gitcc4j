package gitcc.config;

import java.util.HashMap;
import java.util.Map;

public class Config {

	private static final String DEFAULT_MASTER = "master";

	private Map<String, User> users = new HashMap<String, User>();

	private String suffix = "example.com";
	private String branch;
	private boolean debug = true;
	private String[] include = new String[] { "." };
	private String[] branches = new String[] { "main" };
	private String clearcase;
	private String type;
	private String url;
	private String username;
	private String password;
	private String group;
	private String integration;
	private String stream;

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setInclude(String[] include) {
		this.include = include;
	}

	public void setBranches(String[] branches) {
		this.branches = branches;
	}

	public void setClearcase(String clearcase) {
		this.clearcase = clearcase;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSuffix() {
		return suffix;
	}

	public String getCC() {
		return _getBranch() + "_cc";
	}

	public String getCI() {
		return _getBranch() + "_ci";
	}

	public boolean isDebug() {
		return debug;
	}

	public String getBranch() {
		return branch;
	}

	public String _getBranch() {
		return branch != null ? branch : DEFAULT_MASTER;
	}

	public String[] getInclude() {
		return include;
	}

	public String[] getBranches() {
		return branches;
	}

	public String getClearcase() {
		return clearcase;
	}

	public boolean isUCM() {
		return "UCM".equals(type);
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getGroup() {
		return group;
	}

	public String getIntegration() {
		return integration;
	}

	public void setIntegration(String integration) {
		this.integration = integration;
	}

	public String getStream() {
		return stream;
	}

	public void setStream(String stream) {
		this.stream = stream;
	}

	public void addUser(User user) {
		users.put(user.getUsername(), user);
	}

	public User getUser(String author) {
		User user = users.get(author);
		if (user == null)
			user = new User(author, suffix);
		return user;
	}
}
