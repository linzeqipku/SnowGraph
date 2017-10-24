package graphlocater.filters;


import org.apache.commons.lang3.ArrayUtils;

public class FilterDeterminer {
	public static boolean isValuable(String word) {
		return ArrayUtils.contains(Rules.valuable_determiners, word);
	}

}
