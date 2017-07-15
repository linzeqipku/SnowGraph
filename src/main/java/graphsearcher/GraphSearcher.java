package graphsearcher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.tartarus.snowball.ext.EnglishStemmer;

import graphdb.extractors.miners.codeembedding.trans.TransExtractor;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;

public class GraphSearcher {

	Map<Long, List<Double>> id2Vec = new HashMap<>();
	Map<Long, Set<String>> id2Words = new HashMap<>();
	Map<Long, String> id2Sig = new HashMap<>();
	
	public static void main(String[] args){
		String testQuery="sort search results field value";
		String graphPath="E:\\SnowGraphData\\lucene\\graphdb";
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphPath));
		GraphSearcher graphSearcher=new GraphSearcher(db);
		List<String> queryWordList=new ArrayList<>();
		for (String word:testQuery.split("\\s+"))
			queryWordList.add(word.toLowerCase());
		Set<Long> res=graphSearcher.query(queryWordList);
		for (long id:res)
			System.out.println(graphSearcher.id2Sig.get(id));
	}

	public Set<Long> query(List<String> queryWordList) {

		Set<String> queryWordSet = new HashSet<>();
		queryWordSet.addAll(queryWordList);

		Map<String, Set<Long>> word2Nodes = new HashMap<>();
		for (String queryWord : queryWordSet) {
			word2Nodes.put(queryWord, new HashSet<>());
			for (long id : id2Words.keySet())
				for (String word : id2Words.get(id))
					if (match(word, queryWord)) {
						word2Nodes.get(queryWord).add(id);
						break;
					}
		}
		
		int maxCount=0;
		int wordCount=Integer.MAX_VALUE;
		long cNode=-1;
		for (long id:id2Words.keySet()){
			int count=0;
			for (String word:id2Words.get(id))
				for (String queryWord:queryWordSet)
					if (match(word, queryWord)){
						count++;
						break;
					}
			if (count>maxCount||(count==maxCount&&id2Words.get(id).size()<wordCount)){
				maxCount=count;
				wordCount=id2Words.get(id).size();
				cNode=id;
			}
		}
		
		Set<Long> resNodes=new HashSet<>();
		if (maxCount>0){
			for (String queryWord:queryWordSet){
				double minDist=Double.MAX_VALUE;
				long argMinDistNode=0;
				for (long node:word2Nodes.get(queryWord)){
					double dist=dist(node,cNode);
					if (dist<minDist){
						minDist=dist;
						argMinDistNode=node;
					}
				}
				resNodes.add(argMinDistNode);
			}
		}
		
		return resNodes;

	}
	
	public GraphSearcher(GraphDatabaseService db){
		try (Transaction tx = db.beginTx()) {
			ResourceIterable<Node> nodes=db.getAllNodes();
			for (Node node:nodes){
				if (!node.hasLabel(Label.label(JavaCodeExtractor.CLASS))
						&&!node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE))
						&&!node.hasLabel(Label.label(JavaCodeExtractor.METHOD)))
					continue;
				if (!node.hasProperty(TransExtractor.CODE_TRANSE_VEC))
					continue;
				String[] eles=((String)node.getProperty(TransExtractor.CODE_TRANSE_VEC)).trim().split("\\s+");
				List<Double> vec=new ArrayList<Double>();
				for (String e:eles)
					vec.add(Double.parseDouble(e));
				long id=node.getId();
				String sig=(String) node.getProperty(JavaCodeExtractor.SIGNATURE);
				Set<String> words=new HashSet<>();
				String name="";
				if (node.hasLabel(Label.label(JavaCodeExtractor.CLASS)))
					name=(String)node.getProperty(JavaCodeExtractor.CLASS_NAME);
				if (node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE)))
					name=(String)node.getProperty(JavaCodeExtractor.INTERFACE_NAME);
				if (node.hasLabel(Label.label(JavaCodeExtractor.METHOD)))
					name=(String)node.getProperty(JavaCodeExtractor.METHOD_NAME);
				for (String e:name.split("[^A-Za-z]+"))
					for (String word:camelSplit(e)){
						words.add(word);
					}
				id2Vec.put(id, vec);
				id2Words.put(id, words);
				id2Sig.put(id, sig);
			}
			tx.success();
		}
	}

	static boolean match(String word1, String word2) {
		EnglishStemmer stemmer = new EnglishStemmer();
		if (word1.matches("\\w+")){
			stemmer.setCurrent(word1);
			stemmer.stem();
			word1=stemmer.getCurrent();
		}
		if (word2.matches("\\w+")){
			stemmer.setCurrent(word2);
			stemmer.stem();
			word2=stemmer.getCurrent();
		}
		return word1.equals(word2);
	}
	
    static List<String> camelSplit(String e) {
        List<String> r = new ArrayList<String>();
        Matcher m = Pattern.compile("^([a-z]+)|([A-Z][a-z]+)|([A-Z]+(?=([A-Z]|$)))").matcher(e);
        if (m.find()) {
            String s = m.group().toLowerCase();
            r.add(s);
            if (s.length() < e.length())
                r.addAll(camelSplit(e.substring(s.length())));
        }
        return r;
    }
	
	double dist(long node1, long node2){
		double r=0;
		for (int i=0;i<id2Vec.get(node1).size();i++)
			r+=(id2Vec.get(node1).get(i)-id2Vec.get(node2).get(i))*(id2Vec.get(node1).get(i)-id2Vec.get(node2).get(i));
		r=Math.sqrt(r);
		return r;
	}

}