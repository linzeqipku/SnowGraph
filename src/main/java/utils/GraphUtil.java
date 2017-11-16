package utils;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class GraphUtil {
	public static boolean hasRelationShip(Node nodeA, Node nodeB, RelationshipType t) {
		for (Relationship relationship : nodeA.getRelationships(t)) {
			if (relationship.getOtherNode(nodeA).equals(nodeB)) return true;
		}
		return false;
	}

	public static boolean hasRelationShip(Node nodeA, Node nodeB, String relationshipName) {
		return hasRelationShip(nodeA, nodeB, RelationshipType.withName(relationshipName));
	}
}
