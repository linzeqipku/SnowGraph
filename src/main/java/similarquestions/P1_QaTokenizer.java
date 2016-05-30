package similarquestions;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.tartarus.snowball.ext.EnglishStemmer;

import graphmodel.ManageElements;
import graphmodel.entity.Schema;
import similarquestions.utils.SimilarQuestionTaskConfig;

public class P1_QaTokenizer {

	SimilarQuestionTaskConfig config = null;
	
	public static void main(String[] args){
		P1_QaTokenizer p=new P1_QaTokenizer("apache-poi");
		p.run();
	}

	public P1_QaTokenizer(String projectName) {
		config = new SimilarQuestionTaskConfig(projectName);
	}

	public void run(){
		EnglishStemmer stemmer=new EnglishStemmer();
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(config.graphPath));
		try (Transaction tx = db.beginTx()){
			ResourceIterator<Node> nodes=db.getAllNodes().iterator();
			while (nodes.hasNext()){
				Node node=nodes.next();
				if (!node.hasLabel(ManageElements.Labels.QUESTION)&&!node.hasLabel(ManageElements.Labels.ANSWER))
					continue;
				List<String> tokens=new ArrayList<String>();
				String content="<html>"+Schema.getContent(node)+"</html>";
				content=Jsoup.parse(content).text();
				for (String word:content.split("[^A-Za-z]+")){
					if (word.length()==0)
						continue;
					List<String> camelTokens=camelSplit(word);
					// IndexReader --> stem(indexreader)+stem(index)+stem(reader)
					if (camelTokens.size()>1){
						stemmer.setCurrent(word.toLowerCase());
						stemmer.stem();
						tokens.add(stemmer.getCurrent());
					}
					for (String token:camelTokens){
						stemmer.setCurrent(token);
						stemmer.stem();
						tokens.add(stemmer.getCurrent());
					}
				}
				String tokensLine="";
				for (String token:tokens)
					tokensLine+=token+" ";
				tokensLine=tokensLine.trim();
				node.setProperty(SimilarQuestionTaskConfig.TOKENS_LINE, tokensLine);
			}
			tx.success();
		}
		db.shutdown();
	}
	
	static List<String> camelSplit(String e){
		List<String> r = new ArrayList<String>();
		Matcher m = Pattern.compile("^([a-z]+)|([A-Z][a-z]+)|([A-Z]+(?=([A-Z]|$)))").matcher(e);
		if (m.find())
		{
			String s = m.group().toLowerCase();
			r.add(s);
			if (s.length() < e.length())
				r.addAll(camelSplit(e.substring(s.length())));
		}
		return r;
	}

}
