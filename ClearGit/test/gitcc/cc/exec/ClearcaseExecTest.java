package gitcc.cc.exec;

import java.util.List;

import gitcc.cc.CCFile;
import gitcc.cc.CCFile.Status;
import junit.framework.TestCase;

public class ClearcaseExecTest extends TestCase {

	public void test() {
		ClearcaseExec cc = new ClearcaseExec();
		String end = " --11-05T14:40 userid\n";
		String diff = "-----[ added ]-----                        \n"
				+ "> b.x -> blah                                  \n"
				+ ">  space " + end + "                            \n"
				+ "> lib/" + end
				+ "-----[ deleted ]-----                          \n"
				+ "< a.x " + end;
		List<CCFile> files = cc.diff("b", diff);
		check(files.get(0), "b/ space", Status.Added);
		check(files.get(1), "b/lib", Status.Added);
		check(files.get(2), "b/a.x", Status.Deleted);
	}

	private void check(CCFile file, String name, Status status) {
		assertEquals(name, file.getFile());
		assertEquals(status, file.getStatus());
	}

}
