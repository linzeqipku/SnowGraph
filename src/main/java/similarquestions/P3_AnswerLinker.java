package similarquestions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import similarquestions.utils.BM25Similarity;
import similarquestions.utils.SimilarQuestionTaskConfig;

public class P3_AnswerLinker {

	SimilarQuestionTaskConfig config = null;
	GraphDatabaseService db = null;
	
	BM25Similarity sim=new BM25Similarity();
	
	private Set<Node> acAnswerNodes=new HashSet<Node>();
	private Set<Node> sampleAnswerNodes=new HashSet<Node>();
	
	public static void main(String[] args){
		P3_AnswerLinker p=new P3_AnswerLinker("apache-poi");
		p.run();
	}
	
	public P3_AnswerLinker(String projectName){
		config=new SimilarQuestionTaskConfig(projectName);
		db=new GraphDatabaseFactory().newEmbeddedDatabase(new File(config.graphPath));
	}
	
	public void run(){
		clean();
		getAcAnswerNodes();
		getSampleAnswerNodes();
		getIdfMapAndAvgDL();
		linkSimilarAnswers();
	}
	
	private void clean(){
		try (Transaction tx = db.beginTx()){
			ResourceIterator<Relationship> rels=db.getAllRelationships().iterator();
			while (rels.hasNext()){
				Relationship rel=rels.next();
				if (rel.isType(SimilarQuestionTaskConfig.RelTypes.SIMILAR_ANSWER))
					rel.delete();
			}
			tx.success();
		}
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
		System.out.println("共有"+acAnswerNodes.size()+"个被采纳的答案.");
	}
	
	private void getSampleAnswerNodes(){
		try (Transaction tx = db.beginTx()){
			for (Node aNode:acAnswerNodes){
				Node qNode=aNode.getRelationships(ManageElements.RelTypes.HAVE_ANSWER,Direction.INCOMING).iterator().next().getStartNode();
				if (qNode.hasProperty(SimilarQuestionTaskConfig.CODES_LINE)&&aNode.hasProperty(SimilarQuestionTaskConfig.CODES_LINE))
					sampleAnswerNodes.add(aNode);
			}
			tx.success();
		}
		System.out.println("选取了"+sampleAnswerNodes.size()+"个样本.");
	}
	
	private void getIdfMapAndAvgDL(){
		double docNum=0;
		double tokenNum=0;
		Map<String, Double> countMap=new HashMap<String, Double>();
		try (Transaction tx = db.beginTx()){
			ResourceIterator<Node> nodes=db.getAllNodes().iterator();
			while (nodes.hasNext()){
				Node node=nodes.next();
				if (!node.hasLabel(ManageElements.Labels.ANSWER))
					continue;
				if (!(boolean)node.getProperty(AnswerSchema.ACCEPTED))
					continue;
				docNum++;
				String[] tokens=((String)node.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE)).split(" ");
				tokenNum+=tokens.length;
				Set<String> tokenSet=new HashSet<String>();
				for (String token:tokens)
					tokenSet.add(token);
				for (String token:tokenSet){
					if (!countMap.containsKey(token))
						countMap.put(token, 0.0);
					countMap.put(token, countMap.get(token)+1);
				}
			}
			tx.success();
		}
		sim.setAvgDL(tokenNum/docNum);
		Map<String, Double> idfMap=new HashMap<String, Double>();
		for (String token:countMap.keySet()){
			double idf=Math.log((0.5+docNum-countMap.get(token))/(0.5+countMap.get(token)));
			idfMap.put(token, idf);
		}
		sim.setIdfMap(idfMap);
	}
	
	private void linkSimilarAnswers(){
		int T=20;
		for (Node node:sampleAnswerNodes){
			Map<Node, Double> scoreMap=scoreAnswers(node);
			List<Entry<Node, Double>> entries=new ArrayList<Entry<Node, Double>>(scoreMap.entrySet());
			Collections.sort(entries,new Comparator<Entry<Node, Double>>() {
				@Override
				public int compare(Entry<Node, Double> o1, Entry<Node, Double> o2) {
					if (o1.getValue().equals(o2.getValue()))
						return 0;
					return o2.getValue()-o1.getValue()<0?-1:1;
				}
			});
			try (Transaction tx = db.beginTx()){
				for (int i=0;i<T&&i<entries.size();i++){
					Node node2=entries.get(i).getKey();
					Relationship rel=node.createRelationshipTo(node2, SimilarQuestionTaskConfig.RelTypes.SIMILAR_ANSWER);
					rel.setProperty(SimilarQuestionTaskConfig.RANK, i);
				}
				tx.success();
			}
		}
	}
	
	private Map<Node, Double> scoreAnswers(Node node1){
		Map<Node, Double> r=new HashMap<Node,Double>();
		try (Transaction tx = db.beginTx()){
			String line1=(String)node1.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE);
			String cLine1=(String)node1.getProperty(SimilarQuestionTaskConfig.CODES_LINE);
			Set<Long> codeSet1=new HashSet<Long>();
			for (String id:cLine1.trim().split("\\s+"))
				if (id.length()>0)
					codeSet1.add(Long.parseLong(id));
			for (Node node2:acAnswerNodes){
				if (node1==node2)
					continue;
				String line2=(String)node2.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE);
				r.put(node2, sim.sim(line1, line2));
				String cLine2="";
				if (node2.hasProperty(SimilarQuestionTaskConfig.CODES_LINE))
					cLine2=(String)node2.getProperty(SimilarQuestionTaskConfig.CODES_LINE);
				Set<Long> codeSet2=new HashSet<Long>();
				for (String id:cLine2.trim().split("\\s+"))
					if (id.length()>0)
						codeSet2.add(Long.parseLong(id));
				boolean flag=false;
				for (long id:codeSet1)
					if (codeSet2.contains(id))
						flag=true;
				if (!flag)
					r.put(node2, 0.0);
			}
			tx.success();
		}
		return r;
	}
	
}
