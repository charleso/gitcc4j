package gitcc.cc;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import junit.framework.TestCase;

public class DescribeUtilTest extends TestCase {

	public void testGetMerge() throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(getClass()
				.getResourceAsStream("describe.example")));
		StringBuilder b = new StringBuilder();
		for (String line; (line = in.readLine()) != null;)
			b.append(line).append("\n");
		CCFile file = DescribeUtil.getMerge(b.toString());
		assertEquals("/path/to/file@@/main/version/1", file.toString());
		assertNull(DescribeUtil.getMerge(""));
	}
}
