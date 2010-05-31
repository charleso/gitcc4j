package gitcc.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

	public static final String DEFAULT_MASTER = "master";

	private Map<String, User> users = new HashMap<String, User>();

	private String suffix = "example.com";
	private String branch;
	private int debugLevel = 1;
	private String[] include = new String[] { "." };
	private String[] branches = new String[] {};
	private String clearcase;
	private String type;
	private int ignoreLevel;
	private String url;
	private String username;
	private String password;
	private String group;
	private String integration;
	private String stream;
	private String remote;
	private String cleartool = "cleartool";
	private boolean merge;
	
	private String smtp;
	private String sender;
	private String[] recipients;

	private long sleep = 5 * 60 * 1000;
	private long rebaseTime = 5 * 60 * 1000;

	private boolean deliver = true;

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public void setDebugLevel(int level) {
		this.debugLevel = level;
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

	public int getIgnoreLevel() {
		return ignoreLevel;
	}

	public void setIgnoreLevel(int ignoreLevel) {
		this.ignoreLevel = ignoreLevel;
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

	public int getDebugLevel() {
		return debugLevel;
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

	public void setEmailSmtp(String smtp) {
		this.smtp = smtp;
	}

	public String getEmailSmtp() {
		return smtp;
	}

	public String[] getEmailRecipients() {
		return recipients;
	}

	public void setEmailRecipients(String[] recipients) {
		this.recipients = recipients;
	}

	public List<String> getAllEmails() {
		List<String> emails = new ArrayList<String>();
		emails.addAll(Arrays.asList(recipients));
		for (User user : users.values()) {
			emails.add(user.getEmail());
		}
		return emails;
	}

	public String getEmailSender() {
		return sender;
	}

	public void setEmailSender(String sender) {
		this.sender = sender;
	}

	public void setSleep(long sleep) {
		this.sleep = sleep;
	}

	public long getSleep() {
		return sleep;
	}

	public long getRebaseTime() {
		return rebaseTime;
	}

	public void setRebaseTime(long rebaseTime) {
		this.rebaseTime = rebaseTime;
	}

	public void addUser(User user) {
		users.put(user.getUsername(), user);
		users.put(user.getEmail(), user);
	}

	public User getUser(String author) {
		User user = users.get(author);
		author = author + "@" + suffix;
		if (user == null)
			user = getUserByEmail(author);
		if (user == null)
			user = new User(author);
		return user;
	}

	public User getUserByEmail(String author) {
		User user = users.get(author);
		if (user == null)
			user = users.get(author.substring(0, author.indexOf('@')));
		return user;
	}

	public String getRemote() {
		return remote;
	}

	public void setRemote(String remote) {
		this.remote = remote;
	}

	public void setDeliver(boolean deliver) {
		this.deliver = deliver;
	}

	public boolean isDeliver() {
		return deliver;
	}

	public String getCleartool() {
		return cleartool;
	}

	public void setCleartool(String cleartool) {
		this.cleartool = cleartool;
	}

	public void setMerge(boolean merge) {
		this.merge = merge;
	}

	public boolean isMerge() {
		return merge;
	}
}
