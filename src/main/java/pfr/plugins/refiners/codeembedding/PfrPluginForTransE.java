package pfr.plugins.refiners.codeembedding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import pfr.PFR;
import pfr.annotations.PropertyDeclaration;
import pfr.plugins.parsers.javacode.PfrPluginForJavaCode;

public class PfrPluginForTransE implements PFR{
	
	@PropertyDeclaration(parent=PfrPluginForJavaCode.CLASS) public static final String CLASS_TRANSE_VEC="transVec";
	@PropertyDeclaration(parent=PfrPluginForJavaCode.INTERFACE) public static final String INTERFACE_TRANSE_VEC="transVec";
	@PropertyDeclaration(parent=PfrPluginForJavaCode.METHOD) public static final String METHOD_TRANSE_VEC="transVec";
	@PropertyDeclaration(parent=PfrPluginForJavaCode.FIELD) public static final String FIELD_TRANSE_VEC="transVec";
	
	GraphDatabaseService db=null;
	TransE transE=null;
	
	public void run(GraphDatabaseService db){
		this.db=db;
		transE=new TransE();
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
				if (!node.hasLabel(Label.label(PfrPluginForJavaCode.CLASS))&&
						!node.hasLabel(Label.label(PfrPluginForJavaCode.INTERFACE))&&
						!node.hasLabel(Label.label(PfrPluginForJavaCode.METHOD))&&
						!node.hasLabel(Label.label(PfrPluginForJavaCode.FIELD)))
						continue;
				entities.add(""+node.getId());
			}
			
			ResourceIterator<Relationship> rels=db.getAllRelationships().iterator();
			while (rels.hasNext()){
				Relationship rel=rels.next();
				Node node1=rel.getStartNode();
				if (!node1.hasLabel(Label.label(PfrPluginForJavaCode.CLASS))&&
					!node1.hasLabel(Label.label(PfrPluginForJavaCode.INTERFACE))&&
					!node1.hasLabel(Label.label(PfrPluginForJavaCode.METHOD))&&
					!node1.hasLabel(Label.label(PfrPluginForJavaCode.FIELD)))
					continue;
				Node node2=rel.getEndNode();
				if (!node2.hasLabel(Label.label(PfrPluginForJavaCode.CLASS))&&
					!node2.hasLabel(Label.label(PfrPluginForJavaCode.INTERFACE))&&
					!node2.hasLabel(Label.label(PfrPluginForJavaCode.METHOD))&&
					!node2.hasLabel(Label.label(PfrPluginForJavaCode.FIELD)))
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
					setVec(node,line);
				}
				tx.success();
			}
		}
	}
	
	private void setVec(Node node, String line){
		if (node.hasLabel(Label.label(PfrPluginForJavaCode.CLASS)))
			node.setProperty(CLASS_TRANSE_VEC, line);
		if (node.hasLabel(Label.label(PfrPluginForJavaCode.INTERFACE)))
			node.setProperty(INTERFACE_TRANSE_VEC, line);
		if (node.hasLabel(Label.label(PfrPluginForJavaCode.METHOD)))
			node.setProperty(METHOD_TRANSE_VEC, line);
		if (node.hasLabel(Label.label(PfrPluginForJavaCode.FIELD)))
			node.setProperty(FIELD_TRANSE_VEC, line);
	}
	
}
