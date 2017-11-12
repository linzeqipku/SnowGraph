package graphdb.extractors.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenizationUtils {
	
	public static String getNTabs(int n) {
		String result = "";
		for (int i = 0; i < n; ++i) result += "\t";
		return result;
	}
	
	public static List<String> camelSplit(String e) {
		List<String> r = new ArrayList<String>();
		Matcher m = Pattern.compile("^([a-z]+)|([A-Z][a-z]+)|([A-Z]+(?=([A-Z]|$)))").matcher(e);
		if (m.find()) {
			String s = m.group().toLowerCase();
			r.add(s);
			if (s.length() < e.length())
				r.addAll(camelSplit(e.substring(s.length())));
		}
		return r;
	}
	
}
