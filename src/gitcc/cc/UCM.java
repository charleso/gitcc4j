package gitcc.cc;

import gitcc.Log;
import gitcc.config.Config;

import java.util.HashMap;
import java.util.Map;

import com.ibm.rational.clearcase.remote_core.cmds.CreateActivity;
import com.ibm.rational.clearcase.remote_core.cmds.GetMyActivities;
import com.ibm.rational.clearcase.remote_core.cmds.SetCurrentActivity;
import com.ibm.rational.clearcase.remote_core.cmds.properties.GetActivityProperties;
import com.ibm.rational.clearcase.remote_core.cmds.sync.Update;
import com.ibm.rational.clearcase.remote_core.copyarea.CopyArea;
import com.ibm.rational.clearcase.remote_core.integration.DeliverStream;
import com.ibm.rational.clearcase.remote_core.integration.PrepareRebase;
import com.ibm.rational.clearcase.remote_core.integration.RebaseStream;
import com.ibm.rational.clearcase.remote_core.server_entities.description.ICommonActivity;
import com.ibm.rational.clearcase.remote_core.server_entities.description.IHeadlinedUcmActivity;

public class UCM extends ClearcaseImpl {

	private final Map<String, String> activities = new HashMap<String, String>();

	private CopyArea integeration;
	private String stream;

	public UCM(Config config) throws Exception {
		super(config);
		integeration = CopyArea.open(config.getIntegration());
		stream = config.getStream();
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
		run(new PrepareRebase(session, copyArea,
				log(PrepareRebase.Listener.class)));
		UI ui = _rebase(RebaseStream.OperationType.REBASE_START);
		if (ui.inProgress()) {
			ui = _rebase(RebaseStream.OperationType.REBASE_RESUME);
		}
		if (ui.toBeCompleted()) {
			_rebase(RebaseStream.OperationType.REBASE_COMPLETE);
		}
	}

	private UI _rebase(RebaseStream.OperationType type) {
		UI ui = new UI();
		run(new RebaseStream(type, session, log(RebaseStream.UI.class, ui),
				new UpdateListener(), copyArea, null, false, true));
		return ui;
	}

	@Override
	public String mkact(final String message) {
		if (activities.isEmpty()) {
			run(new GetMyActivities(session, copyArea, log(
					GetMyActivities.Listener.class, new Object() {
						@SuppressWarnings("unused")
						public void nextActivity(ICommonActivity act,
								String[] as) {
							activities.put(act.getHeadline(), act.toSelector());
						}
					})));
		}
		final String[] id = new String[] { activities.get(message) };
		if (id[0] == null) {
			run(new CreateActivity(session, copyArea, message, null, null,
					new CreateActivity.Listener() {
						@Override
						public void newActivity(IHeadlinedUcmActivity activity) {
							activities.put(id[0] = activity.getId(), message);
						}
					}));
		} else {
			run(new SetCurrentActivity(session, id[0], copyArea,
					log(SetCurrentActivity.Listener.class)));
		}
		return id[0];
	}

	@Override
	public void rmact(String activity) {
		// TODO Remove activity
	}

	public void deliver() {
		run(new Update(session, new UpdateListener(), integeration,
				HIJACK_TREATMENT, false));
		_deliver(DeliverStream.OperationType.DELIVER_START);
		_deliver(DeliverStream.OperationType.DELIVER_COMPLETE);
		new BaselineUtil(session, stream).run();
	}

	private void _deliver(DeliverStream.OperationType type) {
		DeliverStream.UI _ui = log(DeliverStream.UI.class, new UI());
		run(new DeliverStream(type, session, _ui, integeration, copyArea, true,
				false, null));
	}

	private static class UI {

		private int status;

		public void runComplete(byte b, int i, boolean f) {
			status = b;
		}

		public void statusNotify(int i, String s) {
			Log.debug(s.trim());
		}

		public void configChangeNotify(String s) {
			Log.debug(s.trim());
		}

		public boolean inProgress() {
			return status == 1001;
		}

		public boolean toBeCompleted() {
			return status == 12;
		}
	};
}
