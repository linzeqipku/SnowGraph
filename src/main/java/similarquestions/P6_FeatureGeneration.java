package similarquestions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.cypher.internal.frontend.v2_3.perty.recipe.Pretty.nest;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import graphmodel.ManageElements;
import graphmodel.entity.qa.AnswerSchema;
import similarquestions.utils.Features;
import similarquestions.utils.SimilarQuestionTaskConfig;
import similarquestions.utils.similarity.CodeSimilarity;
import similarquestions.utils.similarity.QueryDocumentSimilarity;
import similarquestions.utils.similarity.Word2VecDocumentSimilarity;

public class P6_FeatureGeneration {
	
	static double ALPHA=0.2;

	SimilarQuestionTaskConfig config = null;
	GraphDatabaseService db = null;
	
	Word2VecDocumentSimilarity word2VecDocumentSimilarity=null;
	QueryDocumentSimilarity queryDocumentSimilarity=new QueryDocumentSimilarity();
	CodeSimilarity codeSimilarity=new CodeSimilarity();
	
	private Set<Node> acQuestionNodes=new HashSet<Node>();
	private Map<Node, Node> qaMap=new HashMap<Node, Node>();
	
	private Set<Node> sampleQuestionNodes=new HashSet<Node>();
	private Map<Node, Set<Node>> standardMap=new HashMap<Node, Set<Node>>();
	
	public static void main(String[] args){
		P6_FeatureGeneration p=new P6_FeatureGeneration("apache-poi");
		p.run();
	}
	
	public P6_FeatureGeneration(String projectName){
		config=new SimilarQuestionTaskConfig(projectName);
		db=new GraphDatabaseFactory().newEmbeddedDatabase(new File(config.graphPath));
	}
	
	public void run(){
		initAcQuestionNodes();
		initSampleQuestionNodes();
		initSimilarity();
		featureGeneration();
	}
	
	private void initAcQuestionNodes(){
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
	
	private void initSampleQuestionNodes(){
		try (Transaction tx = db.beginTx()){
			for (Node qNode:acQuestionNodes){
				Node aNode=qaMap.get(qNode);
				Set<Node> markedCodeSet=new HashSet<Node>();
				Iterator<Relationship> rels=aNode.getRelationships(Direction.OUTGOING).iterator();
				while (rels.hasNext()){
					Relationship rel=rels.next();
					if (rel.hasProperty(SimilarQuestionTaskConfig.MARK)&&(int)rel.getProperty(SimilarQuestionTaskConfig.MARK)==1)
						markedCodeSet.add(rel.getEndNode());
				}
				Set<Node> standardQuestions=new HashSet<Node>();
				for (Node markedCodeNode:markedCodeSet){
					rels=markedCodeNode.getRelationships(ManageElements.RelTypes.DOC_LEVEL_REFER,Direction.INCOMING).iterator();
					while (rels.hasNext()){
						Node node=rels.next().getStartNode();
						if (acQuestionNodes.contains(node))
							standardQuestions.add(node);
						else if (qaMap.values().contains(node)){
							node=node.getRelationships(ManageElements.RelTypes.HAVE_ANSWER,Direction.INCOMING).iterator().next().getStartNode();
							standardQuestions.add(node);
						}
					}
					rels=markedCodeNode.getRelationships(ManageElements.RelTypes.LEX_LEVEL_REFER,Direction.INCOMING).iterator();
					while (rels.hasNext()){
						Node node=rels.next().getStartNode();
						if (acQuestionNodes.contains(node))
							standardQuestions.add(node);
						else if (qaMap.values().contains(node)){
							node=node.getRelationships(ManageElements.RelTypes.HAVE_ANSWER,Direction.INCOMING).iterator().next().getStartNode();
							standardQuestions.add(node);
						}
					}
				}
				standardQuestions.remove(qNode);
				if (!standardQuestions.isEmpty()){
					sampleQuestionNodes.add(qNode);
					standardMap.put(qNode, standardQuestions);
				}
			}
			tx.success();
		}
		System.out.println("选取了"+sampleQuestionNodes.size()+"个样本.");
	}
	
	private void initSimilarity(){
		Map<Long, List<String>> qDocuments=new HashMap<Long, List<String>>();
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
					qDocuments.put(node.getId(), tokenList);
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
		queryDocumentSimilarity.init(new ArrayList<List<String>>(qDocuments.values()));
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
	
	private void featureGeneration(){
		Features features=new Features();
		for (Node sNode:standardMap.keySet()){
			Set<Node> tNodes=standardMap.get(sNode);
			for (Node tNode:tNodes){
				Pair<Long, Long> pair=new ImmutablePair<Long, Long>(sNode.getId(), tNode.getId());
				features.standards.add(pair);
			}
		}
		int c=0;
		for (Node node1:sampleQuestionNodes){
			System.out.println("特征生成进度: "+c+"/"+sampleQuestionNodes.size()+" "+new Date());
			c++;
				try (Transaction tx = db.beginTx()){
					List<String> doc1=split((String)node1.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE));
					List<String> cDoc1=split((String)node1.getProperty(SimilarQuestionTaskConfig.CODES_LINE));
					for (Node node2:acQuestionNodes){
						if (node1.getId()==node2.getId())
							continue;
						List<String> doc2=split((String)node2.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE));
						List<String> cDoc2=split((String)node2.getProperty(SimilarQuestionTaskConfig.CODES_LINE));
						double queryDocSim=queryDocumentSimilarity.sim(doc1, doc2);
						double word2VecSim=word2VecDocumentSimilarity.sim(node1.getId(), node2.getId());
						double codeSimScore=codeSimilarity.sim(cDoc1, cDoc2);
						Pair<Long, Long> pair=new ImmutablePair<Long, Long>(node1.getId(), node2.getId());
						features.surfaceFeature.put(pair, queryDocSim);
						features.word2vecFeature.put(pair, word2VecSim);
						features.code2vecFeature.put(pair, codeSimScore);
					}
					tx.success();
				}
			}
		
		File file=new File(config.featuresPath);
		try {
			OutputStream fos=new FileOutputStream(file);
			ObjectOutputStream out=new ObjectOutputStream(fos);
			out.writeObject(features);
			out.close();
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static List<String> split(String s){
		List<String> r=new ArrayList<String>();
		for (String e:s.split("\\s+"))
			r.add(e);
		return r;
	}
	
}
