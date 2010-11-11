package gitcc.cc.cleartool;

import gitcc.ClearcaseEnvironmentNotSetup;
import gitcc.ClearcaseFactory;
import gitcc.cc.Clearcase;
import gitcc.cc.exec.ClearcaseExec;
import gitcc.config.Config;

public class CleartoolFactory implements ClearcaseFactory {

	@Override
	public Clearcase createClearcase(Config config) throws ClearcaseEnvironmentNotSetup {
		boolean isCleartoolAvailable = ClearcaseExec.isAvailable(config);
		if (!isCleartoolAvailable) {
			throw new ClearcaseEnvironmentNotSetup("executing 'cleartool' command fails");
		}

		if (config.isUCM()) {
			return new gitcc.cc.exec.UCMExec();
		}
		return new gitcc.cc.exec.ClearcaseExec();
	}
}
