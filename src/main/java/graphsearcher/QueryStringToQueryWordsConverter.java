package graphsearcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.tartarus.snowball.ext.EnglishStemmer;

import graphdb.extractors.parsers.word.corpus.WordSegmenter;

public class QueryStringToQueryWordsConverter {
	
	static EnglishStemmer stemmer=new EnglishStemmer();
	static Set<String> englishStopWords=new HashSet<>();
	static Set<String> chineseStopWords=new HashSet<>();
	
	public QueryStringToQueryWordsConverter(){
		List<String> lines=new ArrayList<>();
		try {
			lines=IOUtils.readLines(this.getClass().getResourceAsStream("/stopwords_en.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lines.forEach(n->{
			stemmer.setCurrent(n);
			stemmer.stem();
			englishStopWords.add(stemmer.getCurrent());
		});
		try {
			lines=IOUtils.readLines(this.getClass().getResourceAsStream("/stopwords_cn.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lines.forEach(n->{chineseStopWords.add(n);});
	}
	
	public Set<String> convert(String queryString){
		if (!isChinese(queryString))
			return englishConvert(queryString);
		return chineseConvert(queryString);
	}
	
	Set<String> englishConvert(String queryString){
		Set<String> r=new HashSet<>();
		
		for (String token:queryString.toLowerCase().split("[^a-z]+")){
			if (token.length()<=2)
				continue;
			stemmer.setCurrent(token);
			stemmer.stem();
			token=stemmer.getCurrent();
			if (!englishStopWords.contains(token))
				r.add(token);
		}
		
		return r;
	}
	
	Set<String> chineseConvert(String queryString){
		Set<String> r=new HashSet<>();
		for (String queryWord:WordSegmenter.demo(queryString)){
			if (isChinese(queryWord)&&!chineseStopWords.contains(queryWord))
				r.add(queryWord);
			else {
				if (queryWord.length()<=2)
					continue;
				stemmer.setCurrent(queryWord);
				stemmer.stem();
				queryWord=stemmer.getCurrent();
				if (!englishStopWords.contains(queryWord))
					r.add(queryWord);
			}
		}
		return r;
	}
	
	public static void main(String[] args){
		new QueryStringToQueryWordsConverter().convert("获取所有人的Email信息")
			.forEach(n->{System.out.println(n);});
	}
	
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }
	
}
