package gitcc.cc;

public class DescribeUtil {

	public static CCFile getMerge(String input) {
		String merge = "    Merge <- ";
		for (String line : input.split("\n")) {
			if (line.startsWith(merge)) {
				String split[] = line.substring(merge.length()).split("@@");
				return new CCFile(split[0], split[1], null);
			}
		}
		return null;
	}
}
