package graphdb.extractors.parsers.word.wordbag;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Tokenizer {
	
	private static Pattern title=Pattern.
			compile("^[0-9\\.\u3001\u4E00\u4E8C\u4E09\u56DB\u4E94\u516D\u4E03\u516B\u4E5D\u5341\u96F6\u7B2C\u7AE0]+[0-9\\s\\.\u3001]+([^\\s]+)$");

	public static List<String> token(String s){
		String[] eles=s.split("[\r\n]+");
		List<String> v= new ArrayList<>();
		for (String e:eles){
			e=e.trim();
			Matcher m=title.matcher(e);
			if (m.find())
				v.add(m.group(1));
			else
				v.add(e);
		}
		return v;
	}
	
	public static void main(String[] args){
		for (String title:Tokenizer.token("第一章.返回码、返回信息说明 \r\n\r   	2.1.4原子API \r 2.1、返回码（RTN）"))
			System.out.println(title);
	}
	
}
