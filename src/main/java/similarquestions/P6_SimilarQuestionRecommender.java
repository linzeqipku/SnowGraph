package similarquestions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
import similarquestions.utils.similarity.CodeSimilarity;
import similarquestions.utils.similarity.QueryDocumentSimilarity;
import similarquestions.utils.similarity.Word2VecDocumentSimilarity;

public class P6_SimilarQuestionRecommender {
	
	static double ALPHA=0.2;

	SimilarQuestionTaskConfig config = null;
	GraphDatabaseService db = null;
	
	Word2VecDocumentSimilarity word2VecDocumentSimilarity=null;
	QueryDocumentSimilarity queryDocumentSimilarity=new QueryDocumentSimilarity();
	CodeSimilarity codeSimilarity=new CodeSimilarity();
	
	private Set<Node> acQuestionNodes=new HashSet<Node>();
	private Map<Node, Node> qaMap=new HashMap<Node, Node>();
	private Set<Node> sampleQuestionNodes=new HashSet<Node>();
	
	public static void main(String[] args){
		P6_SimilarQuestionRecommender p=new P6_SimilarQuestionRecommender("apache-poi");
		p.run();
	}
	
	public P6_SimilarQuestionRecommender(String projectName){
		config=new SimilarQuestionTaskConfig(projectName);
		db=new GraphDatabaseFactory().newEmbeddedDatabase(new File(config.graphPath));
	}
	
	public void run(){
		clean();
		getAcQuestionNodes();
		getSampleQuestionNodes();
		initSimilarity();
		linkSimilarQuestions();
	}
	
	private void clean(){
		try (Transaction tx = db.beginTx()){
			ResourceIterator<Relationship> rels=db.getAllRelationships().iterator();
			while (rels.hasNext()){
				Relationship rel=rels.next();
				if (rel.isType(SimilarQuestionTaskConfig.RelTypes.SIMILAR_QUESTION))
					rel.delete();
			}
			tx.success();
		}
	}
	
	private void getAcQuestionNodes(){
		try (Transaction tx = db.beginTx()){
			ResourceIterator<Node> nodes=db.getAllNodes().iterator();
			while (nodes.hasNext()){
				Node node=nodes.next();
				if (!node.hasLabel(ManageElements.Labels.ANSWER))
					continue;
				if (!(boolean)node.getProperty(AnswerSchema.ACCEPTED))
					continue;
				Node qNode=node.getRelationships(ManageElements.RelTypes.HAVE_ANSWER,Direction.INCOMING).iterator().next().getStartNode();
				acQuestionNodes.add(qNode);
				qaMap.put(qNode, node);
			}
			tx.success();
		}
		System.out.println("共有"+acQuestionNodes.size()+"个有被采纳的答案的问题.");
	}
	
	private void getSampleQuestionNodes(){
		try (Transaction tx = db.beginTx()){
			for (Node qNode:acQuestionNodes){
				if (((String)qNode.getProperty(SimilarQuestionTaskConfig.CODES_LINE)).length()==0)
					continue;
				Node aNode=qaMap.get(qNode);
				if (((String)aNode.getProperty(SimilarQuestionTaskConfig.CODES_LINE)).length()==0)
					continue;
				sampleQuestionNodes.add(qNode);
			}
			tx.success();
		}
		System.out.println("选取了"+sampleQuestionNodes.size()+"个样本.");
	}
	
	private void initSimilarity(){
		Map<Long, List<String>> documents=new HashMap<Long, List<String>>();
		List<List<String>> codeCorpus=new ArrayList<List<String>>();
		Map<String, Double[]> vecMap=new HashMap<String, Double[]>();
		try (Transaction tx = db.beginTx()){
			ResourceIterator<Node> nodes=db.getAllNodes().iterator();
			while (nodes.hasNext()){
				Node node=nodes.next();
				
				if (acQuestionNodes.contains(node)){
					//documents
					List<String> tokenList=new ArrayList<String>();
					for (String token:((String)node.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE)).split("\\s+"))
						tokenList.add(token);
					documents.put(node.getId(), tokenList);
				}
				
				if (node.hasProperty(SimilarQuestionTaskConfig.CODES_LINE)){
					//codeCorpus
					List<String> tokenList=new ArrayList<String>();
					for (String token:((String)node.getProperty(SimilarQuestionTaskConfig.CODES_LINE)).split("\\s+"))
						tokenList.add(token);
					codeCorpus.add(tokenList);
				}
				
				if (node.hasProperty(SimilarQuestionTaskConfig.VEC_LINE)){
					//vecMap
					String[] eles=((String)node.getProperty(SimilarQuestionTaskConfig.VEC_LINE)).split("\\s+");
					Double[] vec=new Double[eles.length];
					for (int i=0;i<eles.length;i++){
						vec[i]=Double.parseDouble(eles[i]);
					}
					vecMap.put(""+node.getId(), vec);
				}
				
			}
			tx.success();
		}
		queryDocumentSimilarity.init(new ArrayList<List<String>>(documents.values()));
		try {
			word2VecDocumentSimilarity=new Word2VecDocumentSimilarity(new ObjectInputStream(new FileInputStream(new File(config.word2questionPath))));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		codeSimilarity.setIdfMap(codeCorpus);
		codeSimilarity.setVecMap(vecMap);
		System.out.println("相似度模型载入完毕.");
	}
	
	private void linkSimilarQuestions(){
		int T=20;
		int c=0;
		for (Node node:sampleQuestionNodes){
			System.out.println("相似问题连接进度: "+c+"/"+sampleQuestionNodes.size()+" "+new Date());
			c++;
			Map<Node, Score> scoreMap=scoreQuestions(node);
			List<Pair<Node, Double>> list0=new ArrayList<Pair<Node, Double>>();
			List<Pair<Node, Double>> list1=new ArrayList<Pair<Node, Double>>();
			Set<Node> cfSet=new HashSet<Node>();
			for (Entry<Node, Score> entry:scoreMap.entrySet()){
				list0.add(new ImmutablePair<Node, Double>(entry.getKey(), entry.getValue().v0));
				list1.add(new ImmutablePair<Node, Double>(entry.getKey(), entry.getValue().v1));
				if (entry.getValue().cf)
					cfSet.add(entry.getKey());
			}
			Collections.sort(list0,new TempComparator());
			Collections.sort(list1,new TempComparator());
			Map<Node, Integer> node2Rank0Map=new HashMap<Node, Integer>();
			Map<Node, Integer> node2Rank1Map=new HashMap<Node, Integer>();
			for (int i=0;i<list0.size();i++)
				node2Rank0Map.put(list0.get(i).getKey(), i+1);
			for (int i=0;i<list1.size();i++)
				node2Rank1Map.put(list1.get(i).getKey(), i+1);
			Set<Node> targetNodes=new HashSet<Node>();
			try (Transaction tx = db.beginTx()){
				int linkCount=0,p=0;
				while (linkCount<T&&p<list0.size()){
					Node node2=list0.get(p).getKey();
					if (cfSet.contains(node2)&&!targetNodes.contains(node2)){
						linkCount++;
						int ranks=linkCount;
						int rank0=node2Rank0Map.get(node2);
						int rank1=node2Rank1Map.get(node2);
						Relationship rel=node.createRelationshipTo(node2, SimilarQuestionTaskConfig.RelTypes.SIMILAR_QUESTION);
						rel.setProperty(SimilarQuestionTaskConfig.RANK_S, ranks);
						rel.setProperty(SimilarQuestionTaskConfig.RANK_0, rank0);
						rel.setProperty(SimilarQuestionTaskConfig.RANK_1, rank1);
						targetNodes.add(node2);
					}
					if (linkCount==T)
						break;
					node2=list1.get(p).getKey();
					if (cfSet.contains(node2)&&!targetNodes.contains(node2)){
						linkCount++;
						int ranks=linkCount;
						int rank0=node2Rank0Map.get(node2);
						int rank1=node2Rank1Map.get(node2);
						Relationship rel=node.createRelationshipTo(node2, SimilarQuestionTaskConfig.RelTypes.SIMILAR_QUESTION);
						rel.setProperty(SimilarQuestionTaskConfig.RANK_S, ranks);
						rel.setProperty(SimilarQuestionTaskConfig.RANK_0, rank0);
						rel.setProperty(SimilarQuestionTaskConfig.RANK_1, rank1);
						targetNodes.add(node2);
					}
					p++;
				}
				tx.success();
			}
		}
	}
	
	private Map<Node, Score> scoreQuestions(Node node1){
		Map<Node, Score> r=new HashMap<Node,Score>();
		try (Transaction tx = db.beginTx()){
			List<String> doc1=split((String)node1.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE));
			List<String> cDoc1=split((String)node1.getProperty(SimilarQuestionTaskConfig.CODES_LINE));
			List<String> aCodeDoc1=split((String)qaMap.get(node1).getProperty(SimilarQuestionTaskConfig.CODES_LINE));
			for (Node node2:acQuestionNodes){
				if (node1.getId()==node2.getId())
					continue;
				List<String> doc2=split((String)node2.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE));
				List<String> cDoc2=split((String)node2.getProperty(SimilarQuestionTaskConfig.CODES_LINE));
				List<String> aCodeDoc2=split((String)qaMap.get(node2).getProperty(SimilarQuestionTaskConfig.CODES_LINE));
				double queryDocSim=queryDocumentSimilarity.sim(doc1, doc2);
				double word2VecSim=word2VecDocumentSimilarity.sim(node1.getId(), node2.getId());
				double codeSimScore=codeSimilarity.sim(cDoc1, cDoc2);
				//System.out.println(queryDocSim+" "+word2VecSim+" "+codeSimScore);
				double v0=queryDocSim;
				double v1=(1.0-ALPHA)*word2VecSim+ALPHA*codeSimScore;
				boolean cf=true;
				Set<String> set=new HashSet<String>(aCodeDoc2);
				set.addAll(cDoc2);
				set.retainAll(new HashSet<String>(aCodeDoc1));
				if (set.isEmpty())
					cf=false;
				Score score=new Score(v0, v1, cf);
				r.put(node2, score);
			}
			tx.success();
		}
		return r;
	}
	
	private static List<String> split(String s){
		List<String> r=new ArrayList<String>();
		for (String e:s.split("\\s+"))
			r.add(e);
		return r;
	}
	
}

class Score{
	Score(double v0, double v1, boolean cf){
		this.v0=v0;
		this.v1=v1;
		this.cf=cf;
	}
	double v0,v1;
	boolean cf;
}

class TempComparator implements Comparator<Pair<Node, Double>>{
	@Override
	public int compare(Pair<Node, Double> o1, Pair<Node, Double> o2) {
		if (o1.getValue().equals(o2.getValue()))
			return 0;
		return o2.getValue()-o1.getValue()<0?-1:1;
	}
}
