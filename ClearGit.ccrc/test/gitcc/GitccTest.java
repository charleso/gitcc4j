package gitcc;

import gitcc.cc.exec.ClearcaseExec;
import gitcc.cc.exec.UCMExec;
import gitcc.ccrc.ClearcaseImpl;
import gitcc.ccrc.UCM;
import gitcc.config.Config;
import junit.framework.TestCase;

public class GitccTest extends TestCase {

	public void test() throws Exception {
		createClearcase(ClearcaseImpl.class, false);
		createClearcase(UCM.class, false);
	}

	private void createClearcase(Class<?> type, boolean exec) throws Exception {
		Config config = new Config();
		config.setCleartool(exec ? "dir" : "notarealcommand");
		config.setType(type.getSimpleName().contains("UCM") ? "UCM" : null);
		assertEquals(type, Gitcc.createClearcase(config).getClass());
	}
}
