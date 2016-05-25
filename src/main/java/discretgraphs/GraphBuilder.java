package discretgraphs;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

public abstract class GraphBuilder
{
	
	public String dbPath=null;
	public String name="";

	public GraphBuilder(String dbPath){
		this.dbPath=dbPath;
	}
	
	public abstract void run();
	
	public void migrateTo(String dstPath){
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(dbPath));
		GraphDatabaseService dstDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(dstPath));
		Map<Node,Node> nodeMap=new HashMap<Node,Node>();
		
		try (Transaction tx1 = db.beginTx();Transaction tx2 = dstDb.beginTx()){
			
			ResourceIterator<Node> nodes = GlobalGraphOperations.at(db).getAllNodes().iterator();
			while (nodes.hasNext()){
				Node node=nodes.next();
				Iterator<Label> labels=node.getLabels().iterator();
				if (!labels.hasNext())
					continue;
				Node dupNode=dstDb.createNode();
				nodeMap.put(node, dupNode);
				Iterator<String> keys=node.getPropertyKeys().iterator();
				while (keys.hasNext()){
					String key=keys.next();
					dupNode.setProperty(key, node.getProperty(key));
				}
				while (labels.hasNext()){
					dupNode.addLabel(labels.next());
				}
				
				//若结点不存在uuid，则为其生成uuid
				if(!dupNode.hasProperty("uuid")){
					String uuid = UUID.randomUUID().toString();
					dupNode.setProperty("uuid", uuid);
				}
			}
			
			Iterator<Relationship> rels=GlobalGraphOperations.at(db).getAllRelationships().iterator();
			while (rels.hasNext()){
				Relationship rel=rels.next();
				Node startNode=rel.getStartNode();
				Node endNode=rel.getEndNode();
				if (nodeMap.containsKey(startNode)&&nodeMap.containsKey(endNode))
					nodeMap.get(startNode).createRelationshipTo(nodeMap.get(endNode), rel.getType());
			}
			
			tx1.success();
			tx2.success();
		}
		
		db.shutdown();
		dstDb.shutdown();
	}
	
}
