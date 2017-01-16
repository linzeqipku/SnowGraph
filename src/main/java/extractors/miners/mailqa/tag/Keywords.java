package extractors.miners.mailqa.tag;

import java.util.HashSet;

public class Keywords {

	public static HashSet<String>	functionWordsSet	= new HashSet<String>();

	public static HashSet<String>	projectWordsSet		= new HashSet<String>();

	public static HashSet<String>	otherKeywordsSet	= new HashSet<String>();

	public static HashSet<String>	keywordsSet			= new HashSet<String>();

	public static HashSet<String>	stopWordsSet		= new HashSet<String>();

	public Keywords() {
		getFunctionWordsSet();
		getProjectWordsSet();
		getOtherKeyWordsSet();
	}

	public HashSet<String> getStopWordsSet() {
		if (stopWordsSet.size() == 0) {
			stopWordsSet = StopWords.getStopWordsSet();
		}
		return stopWordsSet;
	}

	public HashSet<String> getFunctionWordsSet() {
		if (functionWordsSet.size() == 0) {
			functionWordsSet = FunctionWords.getFunctionWordsSet();
		}
		return functionWordsSet;
	}

	public HashSet<String> getProjectWordsSet() {
		if (projectWordsSet.size() == 0) {
			projectWordsSet = ProjectWords.getProjectWords();
		}
		return projectWordsSet;
	}

	public HashSet<String> getOtherKeyWordsSet() {
		if (otherKeywordsSet.size() == 0) {
			otherKeywordsSet = OtherKeywords.getOtherKeywords();
		}
		return otherKeywordsSet;
	}

	public HashSet<String> getKeywordsSet() {
		if (keywordsSet.size() == 0) {
			keywordsSet.addAll(getFunctionWordsSet());
			keywordsSet.addAll(getProjectWordsSet());
			keywordsSet.addAll(getOtherKeyWordsSet());
		}

		return keywordsSet;
	}

	public boolean isOtherKeywords(String word) {
		return this.getOtherKeyWordsSet().contains(word);
	}

	public boolean isProjectWords(String word) {
		return this.getProjectWordsSet().contains(word);
	}

	public boolean isFunctionWords(String word) {
		return this.getFunctionWordsSet().contains(word);
	}

	public boolean isKeywords(String word) {
		return this.getKeywordsSet().contains(word);
	}

	public boolean isStopWords(String word) {
		return this.getStopWordsSet().contains(word);
	}

	/**
	 * @Title:Keywords
	 * @Description:add every adj of the title to the key words for each mail
	 * @param text
	 * @return
	 */

	public HashSet<String> updateProjectWordsSet(String text) {

		projectWordsSet.clear();
		if (projectWordsSet.size() == 0) {
			projectWordsSet = ProjectWords.getProjectWords();
		}
		String[] words = text.split(" ");
		for (String word : words) {
			if (isFunctionWords(word) || isFunctionWords(word.toLowerCase())) {
				continue;
			}
			projectWordsSet.add(word);
		}
		return projectWordsSet;
	}
}
