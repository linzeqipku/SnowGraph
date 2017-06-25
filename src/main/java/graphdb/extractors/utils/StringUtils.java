package graphdb.extractors.utils;

public class StringUtils {
	public static String getNTabs(int n) {
		String result = "";
		for (int i = 0; i < n; ++i) result += "\t";
		return result;
	}
}
