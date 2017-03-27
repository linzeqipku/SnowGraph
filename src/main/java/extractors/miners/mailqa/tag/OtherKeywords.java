package extractors.miners.mailqa.tag;

import java.util.HashSet;

public class OtherKeywords {

	public static HashSet<String>	otherKeywords	= new HashSet<String>();

	public static final String[]	QUESTION_WORDS	= { "what", "What", "why", "Why", "which",
			"Which", "when", "When", "where", "Where", "how", "How", "?" };

	public static void addKeywords(String keyword) {
		otherKeywords.add(keyword);
	}

	public static HashSet<String> getOtherKeywords() {
		if (otherKeywords.size() == 0) {
			for (String word : QUESTION_WORDS) {
				otherKeywords.add(word);
			}
		}
		return otherKeywords;
	}

}
