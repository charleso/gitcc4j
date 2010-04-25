package gitcc.cc;

import gitcc.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import com.ibm.rational.clearcase.remote_core.cmd.AbstractCmd;
import com.ibm.rational.clearcase.remote_core.cmds.properties.PropertyCategories;
import com.ibm.rational.clearcase.remote_core.util.Status;

public class BaseClearcase {

	protected <T extends AbstractCmd> T run(T update) {
		try {
			update.run();
		} catch (RuntimeException e) {
			if (e.getMessage().contains("remature EOF")) {
				e.printStackTrace();
				update.getStatus().reset();
				setRun(update, false);
				update.run();
			} else {
				throw e;
			}
		}
		return update;
	}

	private void setRun(AbstractCmd update, boolean run) {
		try {
			Field field = AbstractCmd.class.getDeclaredField("m_wasRun");
			field.setAccessible(true);
			field.set(update, run);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected <T> T log(Class<T> c) {
		return log(c, new Object());
	}

	@SuppressWarnings("unchecked")
	protected <T> T log(Class<T> c, Object o) {
		return (T) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class[] { c }, new LogIH(o));
	}

	protected int toCategoryValue(PropertyCategories... cats) {
		int i = 0;
		for (PropertyCategories cat : cats) {
			i |= cat.toCategoryValue();
		}
		return i;
	}

	private final class LogIH implements InvocationHandler {

		private final Object object;

		public LogIH(Object object) {
			this.object = object;
		}

		@Override
		public Object invoke(Object proxy, Method m, Object[] args)
				throws Throwable {
			if (args == null)
				args = new String[0];
			if (m.getName().equals("runComplete") && args.length == 1) {
				Status status = (Status) args[0];
				if (!status.isOk())
					throw new RuntimeException(status.getMsg());
				return null;
			}
			try {
				return object.getClass().getDeclaredMethod(m.getName(),
						m.getParameterTypes()).invoke(object, args);
			} catch (NoSuchMethodException e) {
				String line = m.getName() + "(" + Arrays.asList(args) + ")";
				Log.trace(line);
				return null;
			}
		}
	}

}
