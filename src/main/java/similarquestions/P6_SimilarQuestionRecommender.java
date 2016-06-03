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
	
	static double ALPHA=0.75;
	static double BETA=0.25;

	SimilarQuestionTaskConfig config = null;
	GraphDatabaseService db = null;
	
	Word2VecDocumentSimilarity word2VecDocumentSimilarity=null;
	QueryDocumentSimilarity queryDocumentSimilarity=new QueryDocumentSimilarity();
	CodeSimilarity codeSimilarity=new CodeSimilarity();
	
	private Set<Node> acQuestionNodes=new HashSet<Node>();
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
				acQuestionNodes.add(node.getRelationships(ManageElements.RelTypes.HAVE_ANSWER,Direction.INCOMING).iterator().next().getStartNode());
			}
			tx.success();
		}
		System.out.println("共有"+acQuestionNodes.size()+"个有被采纳的答案的问题.");
	}
	
	private void getSampleQuestionNodes(){
		try (Transaction tx = db.beginTx()){
			for (Node qNode:acQuestionNodes){
				if (((String)qNode.getProperty(SimilarQuestionTaskConfig.CODES_LINE)).length()>0)
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
			for (Entry<Node, Score> entry:scoreMap.entrySet()){
				list0.add(new ImmutablePair<Node, Double>(entry.getKey(), entry.getValue().v0));
				list1.add(new ImmutablePair<Node, Double>(entry.getKey(), entry.getValue().v1));
			}
			Collections.sort(list0,new TempComparator());
			Collections.sort(list1,new TempComparator());
			Map<Node, Integer> node2Rank0Map=new HashMap<Node, Integer>();
			for (int i=0;i<list0.size();i++)
				node2Rank0Map.put(list0.get(i).getKey(), i+1);
			try (Transaction tx = db.beginTx()){
				for (int i=0;i<T&&i<list1.size();i++){
					Node node2=list1.get(i).getKey();
					double rank0=node2Rank0Map.get(node2);
					double rank1=i+1;
					Relationship rel=node.createRelationshipTo(node2, SimilarQuestionTaskConfig.RelTypes.SIMILAR_QUESTION);
					rel.setProperty(SimilarQuestionTaskConfig.RANK_0, rank0);
					rel.setProperty(SimilarQuestionTaskConfig.RANK_1, rank1);
				}
				tx.success();
			}
		}
	}
	
	private Map<Node, Score> scoreQuestions(Node node1){
		Map<Node, Score> r=new HashMap<Node,Score>();
		try (Transaction tx = db.beginTx()){
			String line1=(String)node1.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE);
			String cLine1=(String)node1.getProperty(SimilarQuestionTaskConfig.CODES_LINE);
			List<String> doc1=new ArrayList<String>();
			for (String token:line1.split("\\s+"))
				doc1.add(token);
			List<String> cDoc1=new ArrayList<String>();
			for (String token:cLine1.split("\\s+"))
				cDoc1.add(token);
			for (Node node2:acQuestionNodes){
				if (node1.getId()==node2.getId())
					continue;
				String line2=(String)node2.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE);
				String cLine2=(String)node2.getProperty(SimilarQuestionTaskConfig.CODES_LINE);
				List<String> doc2=new ArrayList<String>();
				for (String token:line2.split("\\s+"))
					doc2.add(token);
				List<String> cDoc2=new ArrayList<String>();
				for (String token:cLine2.split("\\s+"))
					cDoc2.add(token);
				double queryDocSim=queryDocumentSimilarity.sim(doc1, doc2);
				double word2VecSim=word2VecDocumentSimilarity.sim(node1.getId(), node2.getId());
				double codeSimScore=codeSimilarity.sim(cDoc1, cDoc2);
				//System.out.println(queryDocSim+" "+word2VecSim+" "+codeSimScore);
				double r0=word2VecSim;
				double r1=(1.0-ALPHA-BETA)*queryDocSim+ALPHA*word2VecSim+BETA*codeSimScore;
				Score score=new Score(r0, r1);
				r.put(node2, score);
			}
			tx.success();
		}
		return r;
	}
	
}

class Score{
	Score(double v0, double v1){
		this.v0=v0;
		this.v1=v1;
	}
	double v0,v1;
}

class TempComparator implements Comparator<Pair<Node, Double>>{
	@Override
	public int compare(Pair<Node, Double> o1, Pair<Node, Double> o2) {
		if (o1.getValue().equals(o2.getValue()))
			return 0;
		return o2.getValue()-o1.getValue()<0?-1:1;
	}
}
