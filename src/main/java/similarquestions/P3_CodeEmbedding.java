package similarquestions;

import java.io.File;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import graphmodel.ManageElements;
import similarquestions.utils.PTransE;
import similarquestions.utils.SimilarQuestionTaskConfig;

public class P3_CodeEmbedding {

	SimilarQuestionTaskConfig config = null;
	GraphDatabaseService db = null;
	PTransE pTransE=null;
	
	public static void main(String[] args){
		P3_CodeEmbedding p=new P3_CodeEmbedding("apache-poi");
		p.run();
	}
	
	public P3_CodeEmbedding(String projectName){
		config=new SimilarQuestionTaskConfig(projectName);
		db=new GraphDatabaseFactory().newEmbeddedDatabase(new File(config.graphPath));
		pTransE=new PTransE();
	}
	
	public void run(){
		loadDataToPTransE();
		pTransE.train();
		writeVecLines();
	}
	
	private void loadDataToPTransE(){
		try (Transaction tx = db.beginTx()){
			ResourceIterator<Relationship> rels=db.getAllRelationships().iterator();
			while (rels.hasNext()){
				Relationship rel=rels.next();
				Node node1=rel.getStartNode();
				if (!node1.hasLabel(ManageElements.Labels.CLASS)&&
					!node1.hasLabel(ManageElements.Labels.INTERFACE)&&
					!node1.hasLabel(ManageElements.Labels.METHOD)&&
					!node1.hasLabel(ManageElements.Labels.FIELD))
					continue;
				Node node2=rel.getEndNode();
				if (!node2.hasLabel(ManageElements.Labels.CLASS)&&
					!node2.hasLabel(ManageElements.Labels.INTERFACE)&&
					!node2.hasLabel(ManageElements.Labels.METHOD)&&
					!node2.hasLabel(ManageElements.Labels.FIELD))
					continue;
				pTransE.addTriple(""+node1.getId(), rel.getType().name(), ""+node2.getId());
			}
			tx.success();
		}
		pTransE.init();
	}
	
	private void writeVecLines(){
		Map<String, double[]> embeddings=pTransE.getBestEntityEmbeddings();
		try (Transaction tx = db.beginTx()){
			for (String nodeIdString:embeddings.keySet()){
				Node node=db.getNodeById(Long.parseLong(nodeIdString));
				String line="";
				for (double d:embeddings.get(nodeIdString))
					line+=d+" ";
				line=line.trim();
				node.setProperty(SimilarQuestionTaskConfig.VEC_LINE, line);
			}
			tx.success();
		}
	}
	
}
