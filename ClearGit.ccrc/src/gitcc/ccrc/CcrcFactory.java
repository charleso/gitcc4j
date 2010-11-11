package gitcc.ccrc;

import gitcc.ClearcaseEnvironmentNotSetup;
import gitcc.ClearcaseFactory;
import gitcc.cc.Clearcase;
import gitcc.config.Config;

public class CcrcFactory implements ClearcaseFactory {

	@Override
	public Clearcase createClearcase(Config config) throws ClearcaseEnvironmentNotSetup {
		if (config.isUCM()) {
			return new gitcc.ccrc.UCM();
		}
		return new gitcc.ccrc.ClearcaseImpl();
	}
}
