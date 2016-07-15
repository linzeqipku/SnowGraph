package depressed.similarquestions;

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

import depressed.similarquestions.utils.Features;
import depressed.similarquestions.utils.SimilarQuestionTaskConfig;
import depressed.similarquestions.utils.similarity.CodeSimilarity;
import depressed.similarquestions.utils.similarity.QueryDocumentSimilarity;
import depressed.similarquestions.utils.similarity.Word2VecDocumentSimilarity;
import graphmodel.ManageElements;
import graphmodel.entity.qa.AnswerSchema;

public class P4_FeatureGeneration {
	
	static double ALPHA=0.2;

	SimilarQuestionTaskConfig config = null;
	GraphDatabaseService db = null;

	QueryDocumentSimilarity queryDocumentSimilarity=new QueryDocumentSimilarity();
	CodeSimilarity codeSimilarity=new CodeSimilarity();
	
	private Set<Node> acQuestionNodes=new HashSet<Node>();
	private Map<Node, Node> qaMap=new HashMap<Node, Node>();
	
	private Set<Node> sampleQuestionNodes=new HashSet<Node>();
	
	public static void main(String[] args){
		P4_FeatureGeneration p=new P4_FeatureGeneration("apache-poi");
		p.run();
	}
	
	public P4_FeatureGeneration(String projectName){
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
				if (!qNode.hasProperty(SimilarQuestionTaskConfig.CODES_LINE))
					continue;
				if (((String)qNode.getProperty(SimilarQuestionTaskConfig.CODES_LINE)).trim().length()==0)
					continue;
				if (!aNode.hasProperty(SimilarQuestionTaskConfig.CODES_LINE))
					continue;
				if (((String)aNode.getProperty(SimilarQuestionTaskConfig.CODES_LINE)).trim().length()==0)
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
				
				if (qaMap.keySet().contains(node)||qaMap.values().contains(node)){
					List<String> tokenList=new ArrayList<String>();
					for (String token:((String)node.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE)).split("\\s+"))
						tokenList.add(token);
					documents.put(node.getId(), tokenList);
				}
				
				if (node.hasProperty(SimilarQuestionTaskConfig.CODES_LINE)){
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
		codeSimilarity.setIdfMap(codeCorpus);
		codeSimilarity.setVecMap(vecMap);
		System.out.println("相似度模型载入完毕.");
	}
	
	private void featureGeneration(){
		Features features=new Features();
		int c=0;
		for (Node node1:sampleQuestionNodes){
			System.out.println("特征生成进度: "+c+"/"+sampleQuestionNodes.size()+" "+new Date());
			c++;
				try (Transaction tx = db.beginTx()){
					List<String> doc1=split((String)node1.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE));
					List<String> cDoc1=split((String)node1.getProperty(SimilarQuestionTaskConfig.CODES_LINE));
					for (Node node2:qaMap.values()){
						List<String> doc2=split((String)node2.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE));
						List<String> cDoc2=split((String)node2.getProperty(SimilarQuestionTaskConfig.CODES_LINE));
						double queryDocSim=queryDocumentSimilarity.sim(doc1, doc2);
						double codeSimScore=codeSimilarity.sim(cDoc1, cDoc2);
						Pair<Long, Long> pair=new ImmutablePair<Long, Long>(node1.getId(), node2.getId());
						features.surfaceFeature.put(pair, queryDocSim);
						features.code2vecFeature.put(pair, codeSimScore);
						if (qaMap.get(node1)==node2)
							features.standards.add(pair);
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
