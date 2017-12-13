package utils;

import graphdb.framework.Extractor;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GraphUtil {
	private static boolean hasRelationShip(Node nodeA, Node nodeB, RelationshipType t) {
		for (Relationship relationship : nodeA.getRelationships(t)) {
			if (relationship.getOtherNode(nodeA).equals(nodeB)) return true;
		}
		return false;
	}

	public static boolean hasRelationShip(Node nodeA, Node nodeB, String relationshipName) {
		return hasRelationShip(nodeA, nodeB, RelationshipType.withName(relationshipName));
	}

	public static void buildGraph(String baseGraphPath, String graphPath, List<Extractor> extractors){
		File f = new File(graphPath);
		try {
			FileUtils.deleteDirectory(f);
			if (baseGraphPath != null)
				FileUtils.copyDirectory(new File(baseGraphPath), f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(f);
		for (Extractor extractor : extractors) {
			extractor.run(db);
			System.out.println(extractor.getClass().getName() + " finished. [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]");
		}
		db.shutdown();
	}

}
