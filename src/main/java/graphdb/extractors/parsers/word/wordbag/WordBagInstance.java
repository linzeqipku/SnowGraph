package graphdb.extractors.parsers.word.wordbag;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;

import graphdb.extractors.parsers.word.utils.Config;

public class WordBagInstance {
	
	static HashSet<String> wordBag=null;
	
	static {
		if (wordBag==null)
			try {
				load();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	static String pattern="^[\\s0-9.:\uff1a\u3001\u4E00\u4E8C\u4E09\u56DB\u4E94\u516D\u4E03\u516B\u4E5D\u5341\u96F6\u7B2C\u7AE0]*$";

	public static String getCommon(String content){
		for (String word:wordBag)
			if (content.contains(word)){
				int p=content.indexOf(word);
				String head=content.substring(0, p);
				String tail=content.substring(p+word.length());
				if (head.matches(pattern)&&tail.matches(pattern))
					return word;
			}
		return null;
	}
	
	private static void load() throws IOException{
		List<String> lines=FileUtils.readLines(new File(Config.getWordBagPath()));
		wordBag=new HashSet<String>();
		for (String line:lines)
			wordBag.add(line);
	}
	
}
