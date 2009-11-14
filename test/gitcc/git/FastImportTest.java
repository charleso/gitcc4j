package gitcc.git;

import gitcc.cc.CCCommit;
import gitcc.config.User;
import gitcc.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.axis.utils.ByteArrayOutputStream;

public class FastImportTest extends TestCase {

	public void test() {
		User user = new User("test@example.com");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FastImport fi = new FastImport(out);
		fi.checkout("master");
		Date d = new Date(12345);
		fi.add("a/b/c", 3, new ByteArrayInputStream("xyz".getBytes()));
		fi.remove("d/e/f");
		fi.commit(new CCCommit("abc def", d, "message1"), user);
		assertEquals(getFile("export1.txt"), out.toString());
	}

	private String getFile(String file) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			IOUtils.copy(getClass().getResourceAsStream(file), out);
			return out.toString("utf-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
