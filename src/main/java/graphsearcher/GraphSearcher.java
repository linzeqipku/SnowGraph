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

import graphdb.extractors.miners.codeembedding.line.LINEExtracter;
import graphdb.extractors.parsers.javacode.JavaCodeExtractor;

public class GraphSearcher {
	
	boolean debug=true;
	
	static EnglishStemmer stemmer=new EnglishStemmer();
	static QueryStringToQueryWordsConverter converter=new QueryStringToQueryWordsConverter();
	
	Map<Long, List<Double>> id2Vec = new HashMap<>();
	Map<Long, String> id2Sig = new HashMap<>();
	Map<Long, String> id2Name = new HashMap<>();
	Map<Long, Set<String>> id2Words = new HashMap<>();
	Set<Long> typeSet=new HashSet<>();
	
	Map<String, Set<Long>> word2Ids=new HashMap<>();

	Map<String, Set<Long>> queryWord2Ids=new HashMap<>();
	Set<String> queryWordSet=new HashSet<>();
	
	public static void main(String[] args){
		String testQuery="get all field names in an index reader";
		String graphPath="E:\\SnowGraphData\\lucene\\graphdb";
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphPath));
		GraphSearcher graphSearcher=new GraphSearcher(db);
		Set<Long> res=graphSearcher.query(testQuery);

		for (long id:res)
			System.out.println(graphSearcher.id2Sig.get(id));

	}
	
	void testDist(){
		long node1=0;
		for (long id:typeSet)
			if (id2Name.get(id).equals("indexread"))
				node1=id;
		for (long id:id2Name.keySet())
			if (id2Sig.get(id).contains("org.apache.lucene.index.IndexReader.")){
				System.out.println(id2Sig.get(id)+" "+dist(node1, id));
			}
	}
	
	public GraphSearcher(GraphDatabaseService db){
		try (Transaction tx = db.beginTx()) {
			ResourceIterable<Node> nodes=db.getAllNodes();
			for (Node node:nodes){
				if (!node.hasLabel(Label.label(JavaCodeExtractor.CLASS))
						&&!node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE))
						&&!node.hasLabel(Label.label(JavaCodeExtractor.METHOD)))
					continue;
				if (!node.hasProperty(LINEExtracter.LINE_VEC))
					continue;
				String[] eles=((String)node.getProperty(LINEExtracter.LINE_VEC)).trim().split("\\s+");
				List<Double> vec=new ArrayList<Double>();
				for (String e:eles)
					vec.add(Double.parseDouble(e));
				long id=node.getId();
				String sig=(String) node.getProperty(JavaCodeExtractor.SIGNATURE);
				if (sig.toLowerCase().contains("test"))
					continue;
				String name="";
				if (node.hasLabel(Label.label(JavaCodeExtractor.CLASS)))
					name=(String)node.getProperty(JavaCodeExtractor.CLASS_NAME);
				if (node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE)))
					name=(String)node.getProperty(JavaCodeExtractor.INTERFACE_NAME);
				if (node.hasLabel(Label.label(JavaCodeExtractor.METHOD)))
					name=(String)node.getProperty(JavaCodeExtractor.METHOD_NAME);
				if (node.hasLabel(Label.label(JavaCodeExtractor.METHOD))&&name.matches("[A-Z]\\w+"))
					continue;
				Set<String> words=new HashSet<>();
				for (String e:name.split("[^A-Za-z]+"))
					for (String word:camelSplit(e)){
						word=stem(word);
						if (!word2Ids.containsKey(word))
							word2Ids.put(word, new HashSet<>());
						word2Ids.get(word).add(id);
						words.add(word);
					}
				id2Words.put(id, words);
				id2Vec.put(id, vec);
				id2Sig.put(id, sig);
				if (node.hasLabel(Label.label(JavaCodeExtractor.CLASS))||
						node.hasLabel(Label.label(JavaCodeExtractor.INTERFACE)))
					typeSet.add(id);
				id2Name.put(id, stem(name.toLowerCase()));
			}
			tx.success();
		}
	}
	
	public Set<Long> query(String queryString){

		queryWord2Ids=new HashMap<>();
		queryWordSet=new HashSet<>();
		
		queryWordSet=converter.convert(queryString);
		if (debug){
			System.out.println("queryWordSet = { "+String.join(", ", queryWordSet)+" }");
			System.out.println();
		}

		queryWord2Ids(queryWordSet);
		
		Set<Long> candidateNodes=candidate();
		
		double minCost=Double.MAX_VALUE;
		Set<Long> minCostNodeSet=new HashSet<>();
		for (long cNode:candidateNodes){
			if (debug)
				System.out.println("cNode: "+id2Sig.get(cNode));
			Set<Long> minDistNodeSet=new HashSet<>();
			minDistNodeSet.add(cNode);
			for (String queryWord:queryWordSet){
				if (queryWord2Ids.get(queryWord).contains(cNode))
					continue;
				double minDist=Double.MAX_VALUE;
				long minDistNode=-1;
				for (long node:queryWord2Ids.get(queryWord)){
					double dist=dist(cNode, node);
					if (dist<minDist){
						minDist=dist;
						minDistNode=node;
					}
				}
				minDistNodeSet.add(minDistNode);
				if (debug)
					System.out.println(queryWord+" "+id2Sig.get(minDistNode)+" "+minDist);
			}
			double cost=sumDist(cNode, minDistNodeSet);
			if (cost<minCost){
				minCost=cost;
				minCostNodeSet=minDistNodeSet;
			}
			if (debug)
				System.out.println();
		}
		
		return minCostNodeSet;
	
	}
	
	Set<Long> candidate(){
		
		Set<Long> candidateNodes=new HashSet<>();
		
		for (long node:typeSet)
			if (queryWordSet.contains(id2Name.get(node)))
				candidateNodes.add(node);
		
		for (long node:typeSet){
			double count=0;
			for (String word:id2Words.get(node))
				if (queryWordSet.contains(word))
					count++;
			if (count/id2Words.get(node).size()>=0.75)
				candidateNodes.add(node);
		}
		
		if (candidateNodes.size()>0)
			return candidateNodes;
		
		for (String queryWord:queryWordSet)
			for (long node:queryWord2Ids.get(queryWord)){
				if (!typeSet.contains(node))
					continue;
			}
		return candidateNodes;
	}
	
	double sumDist(long cNode, Set<Long> minDistNodeSet){
		double r=0;
		for (long node:minDistNodeSet){
			double dist=dist(cNode, node);
			r+=dist;
		}
		return r;
	}
	
	void queryWord2Ids(Set<String> queryWordSet){
		for (String queryWord:queryWordSet){
			queryWord2Ids.put(queryWord, new HashSet<>());
			queryWord2Ids.put(queryWord, word2Ids.containsKey(queryWord)?word2Ids.get(queryWord):new HashSet<>());
			for (long node:id2Name.keySet())
				if (id2Name.get(node).equals(queryWord))
					queryWord2Ids.get(queryWord).add(node);
		}
		for (String word:queryWord2Ids.keySet())
			if (queryWord2Ids.get(word).size()>0)
				queryWordSet.add(word);
	}
	
	String stem(String word){
		if (word.matches("\\w+")){
			stemmer.setCurrent(word);
			stemmer.stem();
			word=stemmer.getCurrent();
		}
		return word;
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
