package gitcc.cc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ibm.rational.clearcase.remote_core.cmds.properties.GetStreamProperties;
import com.ibm.rational.clearcase.remote_core.cmds.properties.PropertyCategories;
import com.ibm.rational.clearcase.remote_core.cmds.properties.GetStreamProperties.IStreamGeneralProperties;
import com.ibm.rational.clearcase.remote_core.cmds.ucm.BaselineKind;
import com.ibm.rational.clearcase.remote_core.cmds.ucm.MakeBaseline;
import com.ibm.rational.clearcase.remote_core.cmds.ucm.RecommendBaseline;
import com.ibm.rational.clearcase.remote_core.rpc.Session;
import com.ibm.rational.clearcase.remote_core.server_entities.description.IBaselineDescription;
import com.ibm.rational.clearcase.remote_core.server_entities.identity.HandleFactory;
import com.ibm.rational.clearcase.remote_core.server_entities.identity.IBaselineHandle;
import com.ibm.rational.clearcase.remote_core.server_entities.identity.IStreamHandle;

public class BaselineUtil extends BaseClearcase {

	private final Session session;
	private final String stream;

	public BaselineUtil(Session session, String stream) {
		this.session = session;
		this.stream = stream;
	}

	public void run() {
		IStreamHandle sh = HandleFactory.createStreamHandle(stream);
		final Map<String, IBaselineHandle> baselines = new HashMap<String, IBaselineHandle>();
		final String[] name = new String[1];
		Object listener = new Object() {
			@SuppressWarnings("unused")
			public void generalProperties(IStreamGeneralProperties general) {
				name[0] = general.getStreamName();
			}

			@SuppressWarnings("unused")
			public void oneRecommendedBaseline(IBaselineDescription baseline) {
				baselines.put(baseline.getComponent().getName(), baseline
						.getHandle());
			}
		};
		int which = toCategoryValue(PropertyCategories.STREAM_GENERAL,
				PropertyCategories.STREAM_RECOMMENDED_BASELINE);
		run(new GetStreamProperties(session, sh, log(
				GetStreamProperties.IListener.class, listener), which));
		String now = new SimpleDateFormat("dd_MM_yy.HHmm").format(new Date());
		String basename = name[0] + "_" + now;
		MakeBaseline mk = new MakeBaseline(session,
				log(MakeBaseline.IListener.class), sh, basename, "",
				BaselineKind.INCREMENTAL, false);
		IBaselineDescription[] descs = run(mk).getBaselines();
		if (descs == null || descs.length == 0)
			return;
		for (IBaselineDescription desc : descs) {
			String bname = desc.getComponent().getName();
			if (baselines.containsKey(bname)) {
				baselines.put(bname, desc.getHandle());
			}
		}
		IBaselineHandle[] handles = baselines.values().toArray(
				new IBaselineHandle[baselines.size()]);
		run(new RecommendBaseline(session,
				log(RecommendBaseline.IListener.class), sh, handles));
	}
}
