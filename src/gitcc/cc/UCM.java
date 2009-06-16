package gitcc.cc;

import gitcc.Log;
import gitcc.config.Config;

import com.ibm.rational.clearcase.remote_core.cmds.CreateActivity;
import com.ibm.rational.clearcase.remote_core.cmds.properties.GetActivityProperties;
import com.ibm.rational.clearcase.remote_core.integration.DeliverStream;
import com.ibm.rational.clearcase.remote_core.integration.PrepareRebase;
import com.ibm.rational.clearcase.remote_core.integration.RebaseStream;
import com.ibm.rational.clearcase.remote_core.server_entities.description.IHeadlinedUcmActivity;

public class UCM extends ClearcaseImpl {

	public UCM(Config config) {
		super(config);
	}

	@Override
	protected String getCommentFormat() {
		return "%[activity]Xp";
	}

	@Override
	protected String getRealComment(String activity) {
		final String[] info = new String[1];
		run(new GetActivityProperties(session, copyArea, activity, log(
				GetActivityProperties.Listener.class, new Object() {
					@SuppressWarnings("unused")
					public void generalInfo(String s, String s1, String s2,
							String s3, String s4, String s5, String s6,
							String s7, long l, int i) {
						info[0] = s;
					}
				}), 1));
		return info[0];
	}

	@Override
	public void update() {
		rebase();
	}

	@Override
	public void rebase() {
		if (false) { // TODO DO we need this?
			run(new PrepareRebase(session, copyArea,
					log(PrepareRebase.Listener.class)));
		}
		boolean complete = _rebase(RebaseStream.OperationType.REBASE_START);
		if (complete) {
			_rebase(RebaseStream.OperationType.REBASE_COMPLETE);
		}
	}

	private boolean _rebase(RebaseStream.OperationType type) {
		final int[] status = new int[1];
		Object ui = new Object() {
			@SuppressWarnings("unused")
			public void statusNotify(int i, String s) {
				Log.debug(s.trim());
				status[i] = i;
			}

			@SuppressWarnings("unused")
			public void configChangeNotify(String s) {
				Log.debug(s.trim());
			}
		};
		run(new RebaseStream(type, session, log(RebaseStream.UI.class, ui),
				new UpdateListener(), copyArea, null, false, true));
		System.out.println("Rebase status " + status[0]);
		return status[0] > 0;
	}

	@Override
	public String mkact(String message) {
		final String[] activity = new String[1];
		run(new CreateActivity(session, copyArea, message, message, message,
				new CreateActivity.Listener() {
					@Override
					public void newActivity(IHeadlinedUcmActivity act) {
						activity[0] = act.getId();
					}
				}));
		return activity[0];
	}

	@Override
	public void rmact(String activity) {
		// TODO Remove activity
	}

	public void deliver() {
		boolean complete = _deliver(DeliverStream.OperationType.DELIVER_START);
		if (complete) {
			_deliver(DeliverStream.OperationType.DELIVER_START);
		}
	}

	private boolean _deliver(DeliverStream.OperationType type) {
		run(new DeliverStream(type, session, log(DeliverStream.UI.class),
				copyArea, "", null));
		return false; // TODO
	}
}
