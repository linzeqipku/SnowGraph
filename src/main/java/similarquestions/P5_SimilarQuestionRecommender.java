package similarquestions;

import java.io.File;
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

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.medallia.word2vec.Searcher.UnknownWordException;

import graphmodel.ManageElements;
import graphmodel.entity.qa.AnswerSchema;
import similarquestions.utils.SimilarQuestionTaskConfig;
import similarquestions.utils.similarity.CodeSimilarity;
import similarquestions.utils.similarity.QueryDocumentSimilarity;
import similarquestions.utils.similarity.Word2VecDocumentSimilarity;

public class P5_SimilarQuestionRecommender {

	SimilarQuestionTaskConfig config = null;
	GraphDatabaseService db = null;
	
	Word2VecDocumentSimilarity word2VecDocumentSimilarity=new Word2VecDocumentSimilarity();
	QueryDocumentSimilarity queryDocumentSimilarity=new QueryDocumentSimilarity();
	CodeSimilarity codeSimilarity=new CodeSimilarity();
	
	private Set<Node> acAnswerNodes=new HashSet<Node>();
	private Set<Node> sampleQuestionNodes=new HashSet<Node>();
	
	public static void main(String[] args){
		P5_SimilarQuestionRecommender p=new P5_SimilarQuestionRecommender("apache-poi");
		p.run();
	}
	
	public P5_SimilarQuestionRecommender(String projectName){
		config=new SimilarQuestionTaskConfig(projectName);
		db=new GraphDatabaseFactory().newEmbeddedDatabase(new File(config.graphPath));
	}
	
	public void run(){
		clean();
		getAcAnswerNodes();
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
	
	private void getSampleQuestionNodes(){
		try (Transaction tx = db.beginTx()){
			for (Node aNode:acAnswerNodes){
				Node qNode=aNode.getRelationships(ManageElements.RelTypes.HAVE_ANSWER,Direction.INCOMING).iterator().next().getStartNode();
				if (((String)qNode.getProperty(SimilarQuestionTaskConfig.CODES_LINE)).length()>0)
					sampleQuestionNodes.add(qNode);
			}
			tx.success();
		}
		System.out.println("选取了"+sampleQuestionNodes.size()+"个样本.");
	}
	
	private void initSimilarity(){
		List<List<String>> corpus=new ArrayList<List<String>>();
		List<List<String>> codeCorpus=new ArrayList<List<String>>();
		Map<String, Double[]> vecMap=new HashMap<String, Double[]>();
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
		queryDocumentSimilarity.setIdfMap(corpus);
		word2VecDocumentSimilarity.setIdfMap(corpus);
		word2VecDocumentSimilarity.setWord2VecModel(config.word2vecPath);
		codeSimilarity.setIdfMap(codeCorpus);
		codeSimilarity.setVecMap(vecMap);
		System.out.println("相似度模型载入完毕.");
	}
	
	private void linkSimilarQuestions(){
		int T=20;
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
			try (Transaction tx = db.beginTx()){
				for (int i=0;i<T&&i<entries.size();i++){
					Node node2=entries.get(i).getKey();
					Relationship rel=node.createRelationshipTo(node2, SimilarQuestionTaskConfig.RelTypes.SIMILAR_QUESTION);
					rel.setProperty(SimilarQuestionTaskConfig.RANK, i);
				}
				tx.success();
			}
		}
	}
	
	private Map<Node, Double> scoreQuestions(Node node1){
		Map<Node, Double> r=new HashMap<Node,Double>();
		try (Transaction tx = db.beginTx()){
			String line1=(String)node1.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE);
			String cLine1=(String)node1.getProperty(SimilarQuestionTaskConfig.CODES_LINE);
			List<String> doc1=new ArrayList<String>();
			for (String token:line1.split("\\s+"))
				doc1.add(token);
			List<String> cDoc1=new ArrayList<String>();
			for (String token:cLine1.split("\\s+"))
				cDoc1.add(token);
			for (Node aNode2:acAnswerNodes){
				Node node2=aNode2.getRelationships(ManageElements.RelTypes.HAVE_ANSWER,Direction.INCOMING).iterator().next().getStartNode();
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
				double codeSimScore=codeSimilarity.sim(cDoc1, cDoc2);
				//System.out.println(codeSimScore);
				r.put(node2, queryDocSim+codeSimScore);
			}
			tx.success();
		}
		return r;
	}
	
}
