package similarquestions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import graphmodel.ManageElements;
import similarquestions.utils.SimilarQuestionTaskConfig;
import similarquestions.utils.graphembedding.TransE;

public class P3_CodeEmbedding {

	SimilarQuestionTaskConfig config = null;
	GraphDatabaseService db = null;
	TransE transE=null;
	
	public static void main(String[] args){
		P3_CodeEmbedding p=new P3_CodeEmbedding("apache-poi");
		p.run();
	}
	
	public P3_CodeEmbedding(String projectName){
		config=new SimilarQuestionTaskConfig(projectName);
		db=new GraphDatabaseFactory().newEmbeddedDatabase(new File(config.graphPath));
		transE=new TransE();
	}
	
	public void run(){
		prepare();
		transE.run();
		writeVecLines();
	}
	
	private void prepare(){
		List<String> entities=new ArrayList<String>();
		List<String> relations=new ArrayList<String>();
		List<Triple<String, String, String>> triples=new ArrayList<Triple<String, String, String>>();
		try (Transaction tx = db.beginTx()){
			ResourceIterator<Node> nodes=db.getAllNodes().iterator();
			while (nodes.hasNext()){
				Node node=nodes.next();
				if (!node.hasLabel(ManageElements.Labels.CLASS)&&
						!node.hasLabel(ManageElements.Labels.INTERFACE)&&
						!node.hasLabel(ManageElements.Labels.METHOD)&&
						!node.hasLabel(ManageElements.Labels.FIELD))
						continue;
				entities.add(""+node.getId());
			}
			
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
				triples.add(new ImmutableTriple<String, String, String>(""+node1.getId(), ""+node2.getId(), rel.getType().name()));
				if (!relations.contains(rel.getType().name()))
					relations.add(rel.getType().name());
			}
			tx.success();
		}
		transE.prepare(entities, relations, triples);
	}
	
	private void writeVecLines(){
		Map<String, double[]> embeddings=transE.getEntityVecMap();
		List<String> keys=new ArrayList<String>(embeddings.keySet());
		for (int i=0;i<keys.size();i+=1000){
			try (Transaction tx = db.beginTx()){
				for (int j=0;j<1000;j++){
					if (i+j>=keys.size())
						break;
					String nodeIdString=keys.get(i+j);
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
	
}
