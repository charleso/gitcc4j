package gitcc;

public class Log {

	private static int level;

	public static void debug(String string) {
		if (level >= 1) {
			System.out.println(string);
		}
	}

	public static void info(String info) {
		System.out.println(info);
	}

	public static void setLevel(int level) {
		Log.level = level;
	}

	public static void trace(String string) {
		if (level >= 2) {
			System.out.println(string);
		}
	}
}
