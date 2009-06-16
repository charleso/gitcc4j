package gitcc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

	public static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[4096];
		for (int i; (i = in.read(buffer)) != -1;) {
			out.write(buffer, 0, i);
		}
		in.close();
		out.close();
	}
}
