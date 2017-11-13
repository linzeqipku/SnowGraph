package exps.graphlocater.filters;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Rules {
    public static final int			THRESHOLD				= 0;

	private static final String		QA_PHRASES_FILE			= "qa_phrases.dat";
	private static final String		QA_NOUNS_FILE			= "qa_nouns.dat";
	private static final String		QA_VERBS_FILE			= "qa_verbs.dat";
	private static final String		STOP_NOUNS_FILE			= "stop_nouns.dat";
	private static final String		STOP_PHRASES_FILE		= "stop_phrases.dat";
	private static final String		STOP_VERBS_FILE			= "stop_verbs.dat";
	private static final String		UNLIKE_NOUNS_FILE		= "unlike_nouns.dat";
	private static final String		UNLIKE_VERBS_FILE		= "unlike_verbs.dat";
	private static final String		DETERMINERS_FILE		= "valuable_determiners.dat";

	public static String[]			qa_phrases;
	public static String[]			qa_nouns;
	public static String[]			qa_verbs;
	public static String[]			stop_nouns;
	public static String[]			stop_phrases;
	public static String[]			stop_verbs;
	public static String[]			unlike_nouns;
	public static String[]			unlike_verbs;
	public static String[]			valuable_determiners;

	public static final String[]	BE_VERBS				= { "be", "am", "is", "are", "was", "were",
			"being", "been", "'m", "'s", "'re" };
	public static final String[]	MODAL_VERBS				= { "can", "could", "dare", "may", "might",
			"must", "ought", "shall", "should", "will", "would", "'d", "'ll" };
	public static String[]			HAVE_VERBS				= { "have", "has", "had", "having", "'ve", "'s",
			"'d" };
	public static final String[]	NEGATIVE_WORDS			= { "not", "n't", "never" };
	public static final String[]	DETERMINERS				= { "this", "that", "these", "those" };
	public static final String[]	VALID_PRONOUNS			= { "it", "them" };
	public static final String[]	VALID_TWOLETTER_WORDS	= { "an", "as", "at", "by", "db", "in", "ip",
			"of", "on", "or", "to" };

	public static final String		NAME_NP_NN				= "np-nn";
	public static final String		NAME_NP_NP_NN			= "np-np-nn";
	public static final String		NAME_PP_NN				= "pp-nn";

	public static final String		NP_NN_PATTERN			= " ( NP < /NN.*/" + " = " + NAME_NP_NN
			+ " | !< /NN.*/ < (NP < /NN.*/" + " = " + NAME_NP_NP_NN + ") ) ";								// NP最多下扩两层
	public static final String		PP_PATTERN				= " ( PP < /NN.*/" + " = " + NAME_PP_NN
			+ " | !< /NN.*/ < " + NP_NN_PATTERN + " | !< /NN.*/ !< NP < ( PP < /NN.*/" + " = " + NAME_PP_NN
			+ " | !< /NN.*/ < " + NP_NN_PATTERN + ") ) ";
	public static final String		NP_NN_PP_PATTERN		= " ( NP < (PP < NP) ) ";

	private static final String		DT_PATTERN				= " ( DT <"
			+ Rules.ruleWordsConjuctionForTregex(Rules.DETERMINERS) + " ) ";
	private static final String		PRP_PATTERN				= " ( PRP < "
			+ Rules.ruleWordsConjuctionForTregex(Rules.VALID_PRONOUNS) + " ) ";
	public static final String		NP_PRP_PATTERN			= " ( NP <: " + PRP_PATTERN
			+ " | !</NN.*/ < ( NP <: " + PRP_PATTERN + " ) )";
	public static final String		NP_DT_PATTERN			= " ( NP <: " + DT_PATTERN
			+ " | !</NN.*/ < ( NP <: " + DT_PATTERN + " ) )";

	static {
	    loadWordList();
    }

    public static String[] readFile(String filepath){
        BufferedReader reader;
		List<String> tempList = new ArrayList<>();;
		// stop-verbs
		try {
			reader = new BufferedReader(new FileReader(new File(filepath)));
			String line;
			while ((line = reader.readLine()) != null) {
				if (!"".equals(line)) {
					tempList.add(line);
					// 首字母大写
					tempList.add(WordUtils.capitalize(line));
					// 全部大写
					tempList.add(line.toUpperCase());
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tempList.toArray(new String[tempList.size()]);
    }

    public static void loadWordList() {
		String dir = "src/main/resources/rules" + File.separator;
		// stop-verbs
        stop_verbs = readFile(dir + STOP_VERBS_FILE);
		// unlike-verbs
        unlike_verbs = readFile(dir + UNLIKE_VERBS_FILE);
		// stop-nouns
		stop_nouns = readFile(dir + STOP_NOUNS_FILE);
		// unlike-nouns
        unlike_nouns = readFile(dir + UNLIKE_NOUNS_FILE);
		// qa-verbs
        qa_verbs = readFile(dir + QA_VERBS_FILE);
		// qa-nouns
        qa_nouns = readFile(dir + QA_NOUNS_FILE);
		// context-phrases
        qa_phrases = readFile(dir + QA_PHRASES_FILE);
		// stop-phrases
        stop_phrases = readFile(dir + STOP_PHRASES_FILE);
		// valuable determiners
        valuable_determiners = readFile(dir + DETERMINERS_FILE);
	}


	private static String capitalizePhrase(String phrase) {
		if (phrase != null && phrase.length() >= 1) {
			char c = phrase.charAt(0);
			if (CharUtils.isAsciiAlphaLower(c))
				c = (char) (c + 'A' - 'a');
			return c + phrase.substring(1);
		}
		return phrase;
	}

	public static String ruleWordsConjuctionForTregex(String words[]) {
		StringBuilder conj = new StringBuilder();
		for (int i = 0; i < words.length; i++) {
			String keyword = words[i];
			if (i > 0)
				conj.append("|");
			conj.append(keyword);
		}
		return conj.toString();
	}
}
