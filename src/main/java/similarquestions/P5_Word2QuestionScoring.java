package similarquestions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import graphmodel.ManageElements;
import graphmodel.entity.qa.AnswerSchema;
import similarquestions.utils.SimilarQuestionTaskConfig;
import similarquestions.utils.similarity.Word2VecDocumentSimilarity;

public class P5_Word2QuestionScoring {

	SimilarQuestionTaskConfig config = null;
	GraphDatabaseService db = null;
	
	Word2VecDocumentSimilarity word2VecDocumentSimilarity=null;
	
	private Set<Node> acQuestionNodes=new HashSet<Node>();
	private Set<Node> sampleQuestionNodes=new HashSet<Node>();
	
	public static void main(String[] args){
		P5_Word2QuestionScoring p=new P5_Word2QuestionScoring("apache-poi");
		p.run();
	}
	
	public P5_Word2QuestionScoring(String projectName){
		config=new SimilarQuestionTaskConfig(projectName);
		db=new GraphDatabaseFactory().newEmbeddedDatabase(new File(config.graphPath));
	}
	
	public void run(){
		getAcQuestionNodes();
		getSampleQuestionNodes();
		getSimilarity();
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
	
	private void getSimilarity(){
		Map<Long, List<String>> documents=new HashMap<Long, List<String>>();
		try (Transaction tx = db.beginTx()){
			ResourceIterator<Node> nodes=db.getAllNodes().iterator();
			while (nodes.hasNext()){
				Node node=nodes.next();
				if (acQuestionNodes.contains(node)){
					List<String> tokenList=new ArrayList<String>();
					for (String token:((String)node.getProperty(SimilarQuestionTaskConfig.TOKENS_LINE)).split("\\s+"))
						tokenList.add(token);
					documents.put(node.getId(), tokenList);
				}
			}
			tx.success();
		}
		word2VecDocumentSimilarity=new Word2VecDocumentSimilarity(documents, config.word2vecPath);
		
		File file=new File(config.word2questionPath);
		try {
			OutputStream fos=new FileOutputStream(file);
			ObjectOutputStream out=new ObjectOutputStream(fos);
			word2VecDocumentSimilarity.writeObject(out);
			out.close();
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
