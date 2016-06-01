package similarquestions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import similarquestions.utils.BM25Similarity;
import similarquestions.utils.SimilarQuestionTaskConfig;

public class PINF_LinkDecorator {

	SimilarQuestionTaskConfig config = null;
	GraphDatabaseService db = null;
	private static double ALPHA=1;
	
	BM25Similarity sim=new BM25Similarity();
	
	private Set<Node> acAnswerNodes=new HashSet<Node>();
	private Set<Node> sampleAnswerNodes=new HashSet<Node>();
	private Map<Long, Double[]> vecMap=new HashMap<Long, Double[]>();
	
	public static void main(String[] args){
		PINF_LinkDecorator p=new PINF_LinkDecorator("apache-poi");
		p.run();
	}
	
	public PINF_LinkDecorator(String projectName){
		config=new SimilarQuestionTaskConfig(projectName);
		db=new GraphDatabaseFactory().newEmbeddedDatabase(new File(config.graphPath));
	}
	
	public void run(){
		getAcAnswerNodes();
		getSampleAnswerNodes();
		getVecMap();
		getIdfMapAndAvgDL();
		decorateLinks();
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
	
	private void getVecMap(){
		try (Transaction tx = db.beginTx()){
			ResourceIterator<Node> nodes=db.getAllNodes().iterator();
			while (nodes.hasNext()){
				Node node=nodes.next();
				if (!node.hasProperty(SimilarQuestionTaskConfig.VEC_LINE))
					continue;
				String[] eles=((String)node.getProperty(SimilarQuestionTaskConfig.VEC_LINE)).split("\\s+");
				Double[] vec=new Double[eles.length];
				for (int i=0;i<eles.length;i++)
					vec[i]=Double.parseDouble(eles[i]);
				vecMap.put(node.getId(), vec);
			}
			tx.success();
		}
	}
	
	private static double vecSim(Double[] doubles1, Double[] doubles2){
		if (doubles1==null||doubles2==null||doubles1.length!=doubles2.length)
			return 0;
		double r=0;
		for (int i=0;i<doubles1.length;i++)
			r+=doubles1[i]*doubles2[i];
		r=0.5+0.5*r;
		return r;
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
				Node qNode=node.getRelationships(ManageElements.RelTypes.HAVE_ANSWER,Direction.INCOMING).iterator().next().getStartNode();
				String[] tokens=((String)qNode.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE)).split(" ");
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
	
	private void decorateLinks(){
		for (Node node:sampleAnswerNodes){
			Map<Node, Integer> scoreMap0=scoreAnswers(node,false);
			Map<Node, Integer> scoreMap1=scoreAnswers(node,true);
			try (Transaction tx = db.beginTx()){
				Iterator<Relationship> rels=node.getRelationships(SimilarQuestionTaskConfig.RelTypes.SIMILAR_ANSWER,Direction.OUTGOING).iterator();
				while (rels.hasNext()){
					Relationship rel=rels.next();
					rel.setProperty(SimilarQuestionTaskConfig.RANK_1, scoreMap1.get(rel.getEndNode()));
					rel.setProperty(SimilarQuestionTaskConfig.RANK_0, scoreMap0.get(rel.getEndNode()));
				}
				tx.success();
			}
		}
	}
	
	private Map<Node, Integer> scoreAnswers(Node aNode1,boolean withCode){
		Map<Node, Double> scoreMap=new HashMap<Node,Double>();
		try (Transaction tx = db.beginTx()){
			Node qNode1=aNode1.getRelationships(ManageElements.RelTypes.HAVE_ANSWER,Direction.INCOMING).iterator().next().getStartNode();
			String line1=(String)qNode1.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE);
			String cLine1=(String)qNode1.getProperty(SimilarQuestionTaskConfig.CODES_LINE);
			Set<Long> codeSet1=new HashSet<Long>();
			for (String id:cLine1.trim().split("\\s+"))
				if (id.length()>0)
					codeSet1.add(Long.parseLong(id));
			for (Node aNode2:acAnswerNodes){
				if (aNode1==aNode2)
					continue;
				Node qNode2=aNode2.getRelationships(ManageElements.RelTypes.HAVE_ANSWER,Direction.INCOMING).iterator().next().getStartNode();
				String line2=(String)qNode2.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE);
				String cLine2="";
				if (qNode2.hasProperty(SimilarQuestionTaskConfig.CODES_LINE))
					cLine2=(String)qNode2.getProperty(SimilarQuestionTaskConfig.CODES_LINE);
				Set<Long> codeSet2=new HashSet<Long>();
				for (String id:cLine2.trim().split("\\s+"))
					if (id.length()>0)
						codeSet2.add(Long.parseLong(id));
				scoreMap.put(aNode2, sim.sim(line1, line2)+(withCode?1.0:0)*codeSim(codeSet1, codeSet2)*ALPHA);
			}
			tx.success();
		}
		List<Entry<Node, Double>> list=new ArrayList<Entry<Node, Double>>(scoreMap.entrySet());
		Collections.sort(list,new Comparator<Entry<Node, Double>>() {
			@Override
			public int compare(Entry<Node, Double> o1, Entry<Node, Double> o2) {
				if (o1.getValue().equals(o2.getValue()))
					return 0;
				return o2.getValue()-o1.getValue()<0?-1:1;
			}
		});
		Map<Node, Integer> r=new HashMap<Node,Integer>();
		for (int rank=0;rank<list.size();rank++){
			Entry<Node, Double> entry=list.get(rank);
			r.put(entry.getKey(), rank);
		}
		return r;
	}
	
	private double codeSim(Set<Long> codeSet1, Set<Long> codeSet2){
		double r=0;
		for (long id1:codeSet1){
			double max=0;
			for (long id2:codeSet2){
				double s=vecSim(vecMap.get(id1), vecMap.get(id2));
				if (s>max)
					max=s;
			}
			r+=max;
		}
		return r;
	}
	
}
