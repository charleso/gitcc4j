package gitcc.cc.exec;

import junit.framework.TestCase;

public class UCMExecTest extends TestCase {

	public void test() {
		UCMExec cc = new UCMExec();
		String out = "Created activity \"new_activity\".\n"
				+ "Set activity \"new_activity\" in view \"java_int\".\n";
		assertEquals("new_activity", cc.parseMkact(out));
	}
}
