package gitcc.cc.exec;

import gitcc.Log;
import gitcc.util.ExecException;

public class UCMExec extends ClearcaseExec {

	@Override
	public void deliver() {
		exec("deliver", "-f");
		exec("deliver", "-com", "-f");
	}

	@Override
	public void makeBaseline() {
		// TODO Auto-generated method stub
	}

	@Override
	public String getRealComment(String activity) {
		return exec("lsactivity", "-fmt", "%[headline]p", activity);
	}

	@Override
	public String mkact(String message) {
		return parseMkact(exec("mkact", "-f", "-headline", message));
	}

	protected String parseMkact(String out) {
		out = out.split("\n")[0];
		return out.substring(out.indexOf("\"") + 1, out.length() - 2);
	}

	@Override
	public void rebase() {
		String out = exec("rebase", "-rec", "-f");
		if (!out.startsWith("No rebase needed")) {
			Log.debug(out);
			exec("rebase", "-complete");
		}
	}

	@Override
	public void rmact(String activity) {
		try {
			exec("rmactivity", "-f", activity);
		} catch (ExecException e) {
			Log.debug(e.getMessage());
		}
	}

	@Override
	public void update() {
		rebase();
	}

}
