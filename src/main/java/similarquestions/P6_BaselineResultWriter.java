package similarquestions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import graphmodel.ManageElements;
import graphmodel.entity.qa.AnswerSchema;
import similarquestions.utils.SimilarQuestionTaskConfig;
import similarquestions.utils.similarity.QueryDocumentSimilarity;

public class P6_BaselineResultWriter {

	SimilarQuestionTaskConfig config = null;
	GraphDatabaseService db = null;
	QueryDocumentSimilarity queryDocumentSimilarity=new QueryDocumentSimilarity();
	
	private Set<Node> acAnswerNodes=new HashSet<Node>();
	private Set<Node> sampleQuestionNodes=new HashSet<Node>();
	
	public static void main(String[] args){
		P6_BaselineResultWriter p=new P6_BaselineResultWriter("apache-poi");
		p.run();
	}
	
	public P6_BaselineResultWriter(String projectName){
		config=new SimilarQuestionTaskConfig(projectName);
		db=new GraphDatabaseFactory().newEmbeddedDatabase(new File(config.graphPath));
	}
	
	public void run(){
		getAcAnswerNodes();
		getSampleQuestionNodes();
		initSimilarity();
		writeBaselineResult();
	}
	
	private void getAcAnswerNodes(){
		try (Transaction tx = db.beginTx()){
			ResourceIterator<Node> nodes=db.getAllNodes().iterator();
			while (nodes.hasNext()){
				Node node=nodes.next();
				if (!node.hasLabel(ManageElements.Labels.ANSWER))
					continue;
				if (!(boolean)node.getProperty(AnswerSchema.ACCEPTED))
					continue;
				acAnswerNodes.add(node);
			}
			tx.success();
		}
	}
	
	private void getSampleQuestionNodes(){
		try (Transaction tx = db.beginTx()){
			ResourceIterator<Node> nodes=db.getAllNodes().iterator();
			while (nodes.hasNext()){
				Node node=nodes.next();
				if (node.getRelationships(SimilarQuestionTaskConfig.RelTypes.SIMILAR_QUESTION,Direction.OUTGOING).iterator().hasNext())
					sampleQuestionNodes.add(node);
			}
			tx.success();
		}
	}
	
	private void initSimilarity(){
		List<List<String>> corpus=new ArrayList<List<String>>();
		try (Transaction tx = db.beginTx()){
			ResourceIterator<Node> nodes=db.getAllNodes().iterator();
			while (nodes.hasNext()){
				Node node=nodes.next();
				if (node.hasProperty(SimilarQuestionTaskConfig.TOKENS_LINE)){
					//corpus
					List<String> tokenList=new ArrayList<String>();
					for (String token:((String)node.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE)).split("\\s+"))
						tokenList.add(token);
					corpus.add(tokenList);
				}
			}
			tx.success();
		}
		queryDocumentSimilarity.setIdfMap(corpus);
		System.out.println("相似度模型载入完毕.");
	}
	
	private void writeBaselineResult(){
		int c=0;
		for (Node node:sampleQuestionNodes){
			System.out.println("进度: "+c+"/"+sampleQuestionNodes.size()+" "+new Date());
			c++;
			Map<Node, Double> scoreMap=scoreQuestions(node);
			List<Entry<Node, Double>> entries=new ArrayList<Entry<Node, Double>>(scoreMap.entrySet());
			Collections.sort(entries,new Comparator<Entry<Node, Double>>() {
				@Override
				public int compare(Entry<Node, Double> o1, Entry<Node, Double> o2) {
					if (o1.getValue().equals(o2.getValue()))
						return 0;
					return o2.getValue()-o1.getValue()<0?-1:1;
				}
			});
			Map<Node, Integer> rankMap=new HashMap<Node, Integer>();
			for (int i=0;i<entries.size();i++){
				Node node2=entries.get(i).getKey();
				rankMap.put(node2, i+1);
			}
			try (Transaction tx = db.beginTx()){
				Iterator<Relationship> rels=node.getRelationships(SimilarQuestionTaskConfig.RelTypes.SIMILAR_QUESTION,Direction.OUTGOING).iterator();
				while (rels.hasNext()){
					Relationship rel=rels.next();
					Node node2=rel.getEndNode();
					rel.setProperty(SimilarQuestionTaskConfig.RANK_0, rankMap.get(node2));
					rel.setProperty(SimilarQuestionTaskConfig.RANK_1, ((int)rel.getProperty(SimilarQuestionTaskConfig.RANK))+1);
				}
				tx.success();
			}
		}
	}
	
	private Map<Node, Double> scoreQuestions(Node node1){
		Map<Node, Double> r=new HashMap<Node,Double>();
		try (Transaction tx = db.beginTx()){
			String line1=(String)node1.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE);
			List<String> doc1=new ArrayList<String>();
			for (String token:line1.split("\\s+"))
				doc1.add(token);
			for (Node aNode2:acAnswerNodes){
				Node node2=aNode2.getRelationships(ManageElements.RelTypes.HAVE_ANSWER,Direction.INCOMING).iterator().next().getStartNode();
				if (node1.getId()==node2.getId())
					continue;
				String line2=(String)node2.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE);
				List<String> doc2=new ArrayList<String>();
				for (String token:line2.split("\\s+"))
					doc2.add(token);
				double queryDocSim=queryDocumentSimilarity.sim(doc1, doc2);
				r.put(node2, queryDocSim);
			}
			tx.success();
		}
		return r;
	}
	
}
