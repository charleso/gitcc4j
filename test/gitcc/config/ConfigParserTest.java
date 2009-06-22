package gitcc.config;

import java.io.StringReader;
import java.util.Arrays;

import junit.framework.TestCase;

public class ConfigParserTest extends TestCase {

	public void test() throws Exception {
		Config config = new Config();
		String sconfig = "[core]             \n"
				+ "#group = ignoreme         \n"
				+ "branches = b|c            \n"
				+ "url = http://test.com/    \n"
				+ "[master]                  \n"
				+ "clearcase = a             \n"
				+ "[test]                    \n"
				+ "clearcase = b             \n"
				+ "                          \n";
		new ConfigParser().parseConfig(config, new StringReader(sconfig));
		assertEquals("http://test.com/", config.getUrl());
		assertEquals("[b, c]", Arrays.asList(config.getBranches()).toString());
		assertEquals("a", config.getClearcase());
		assertNull(config.getGroup());
	}

	public void testProcessName() {
		assertEquals("foo", ConfigParser.processName("foo"));
		assertEquals("fooBar", ConfigParser.processName("foo.bar"));
		assertEquals("fooBarFoo", ConfigParser.processName("foo.bar.foo"));
	}

	public void testUsers() throws Exception {
		Config config = new Config();
		String sconfig = "user1|Full Name|email@test.com|password\n"
				+ "user2|Another name|foo@bah.com|hello";
		new ConfigParser().parseUsers(config, new StringReader(sconfig));
		User user = config.getUser("user1");
		assertEquals("Full Name", user.getName());
		assertEquals("email@test.com", user.getEmail());
		assertEquals("password", user.getPassword());
		assertEquals("hello", config.getUser("user2").getPassword());
		assertEquals("baduser@example.com", config.getUser("baduser")
				.getEmail());
	}
}
