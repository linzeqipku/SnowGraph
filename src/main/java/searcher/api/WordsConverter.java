package searcher.api;

import graphdb.extractors.parsers.word.corpus.Translator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.tartarus.snowball.ext.EnglishStemmer;
import webapp.SnowGraphContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class WordsConverter {

	private static Set<String> englishStopWords=new HashSet<>();

	public static String stem(String token){
		EnglishStemmer stemmer=new EnglishStemmer();
		stemmer.setCurrent(token);
		try {
			stemmer.stem();
			token=stemmer.getCurrent();
		} catch (IllegalArgumentException e){
		}
		return token;
	}

	public static Set<String> convert(String queryString){
		if (!isChinese(queryString))
			return englishConvert(queryString);
		return chineseConvert(queryString);
	}

	public static Set<String> convertWithoutStem(String queryString){

		if (isChinese(queryString))
			try {
				queryString=Translator.ch2en(queryString);
			} catch (IOException e) {
				e.printStackTrace();
			}
		//System.out.println(queryString);

		Set<String> r=new HashSet<>();

		for (String token:queryString.toLowerCase().split("[^a-z]+")){
			if (token.length()<=2) // 去掉长度小于2的token
				continue;
			if (!englishStopWords.contains(token)) // 去掉停用词
				r.add(token);
		}
		return r;
	}

	public static Set<String> englishConvert(String queryString){
		EnglishStemmer stemmer=new EnglishStemmer();
		Set<String> r=new HashSet<>();

		for (String token:queryString.toLowerCase().split("[^a-z]+")){
			if (token.length()<=2) // 去掉长度小于2的token
				continue;
			stemmer.setCurrent(token);
			try {
				stemmer.stem();
				token=stemmer.getCurrent();
			} catch (IllegalArgumentException e){

			}
			if (!englishStopWords.contains(token)) // 去掉停用词
				r.add(token);
		}

		return r;
	}

	private static Set<String> chineseConvert(String queryString){
		Set<String> r=null;
		try {
			r=englishConvert(Translator.ch2en(queryString));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return r;
	}

    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
	}

    private static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
		for (char c : ch) {
			if (isChinese(c)) {
				return true;
			}
		}
        return false;
    }

	static{
		loadStopWords();
	}
	private static void loadStopWords(){
		EnglishStemmer stemmer=new EnglishStemmer();
		List<String> lines=new ArrayList<>();
		try {
			lines=IOUtils.readLines(SnowGraphContext.class.getResource("/stopwords.txt").openStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		lines.forEach(n->{
			englishStopWords.add(n);
			stemmer.setCurrent(n);
			stemmer.stem();
			englishStopWords.add(stemmer.getCurrent());
		});

	}
}
