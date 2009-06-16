package gitcc;

public class Log {

	private static boolean debug;

	public static void debug(String string) {
		if (debug) {
			System.out.println(string);
		}
	}

	public static void setDebug(boolean debug2) {
		Log.debug = debug2;
	}
}
