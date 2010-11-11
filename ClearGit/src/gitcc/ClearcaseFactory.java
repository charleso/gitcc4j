package gitcc;

import gitcc.cc.Clearcase;
import gitcc.config.Config;

public interface ClearcaseFactory {

	Clearcase createClearcase(Config config) throws ClearcaseEnvironmentNotSetup;

}
