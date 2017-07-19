package graphsearcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.tartarus.snowball.ext.EnglishStemmer;

public class QueryStringToQueryWordsConverter {
	
	static EnglishStemmer stemmer=new EnglishStemmer();
	static Set<String> stopWords=new HashSet<>();
	
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
			stopWords.add(stemmer.getCurrent());
		});
	}
	
	public Set<String> convert(String queryString){
		Set<String> r=new HashSet<>();
		
		for (String token:queryString.toLowerCase().split("[^a-z]+")){
			if (token.length()<=2)
				continue;
			stemmer.setCurrent(token);
			stemmer.stem();
			token=stemmer.getCurrent();
			if (!stopWords.contains(token))
				r.add(token);
		}
		
		return r;
	}
	
	public static void main(String[] args){
		new QueryStringToQueryWordsConverter().convert("how to sort search results based on a field value in lucene-3.0.2?")
			.forEach(n->{System.out.println(n);});
	}
	
}
