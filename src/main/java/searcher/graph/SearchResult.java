package searcher.graph;

import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;

public class SearchResult {
	
	public Set<Long> nodes=new HashSet<>();
	public Set<Long> edges=new HashSet<>();
	public double cost=0;

	public JSONObject toJSON(GraphDatabaseService db){
		if(nodes.size() == 0)
			return null;

		JSONArray nodesArray = new JSONArray();
		JSONArray relationshipsArray = new JSONArray();
		JSONObject graph = new JSONObject();

		JSONArray dataArray = new JSONArray();
		JSONArray resultsArray = new JSONArray();
		JSONObject returnResult = new JSONObject();

		//region add nodes
		for(long nodeID : nodes){
			Node node;
			try(Transaction tx = db.beginTx();
			){
				node = db.getNodeById(nodeID);
				JSONObject temp = new JSONObject();
				temp.put("id" , nodeID);
				temp.put("labels" , node.getLabels().iterator().next().toString());
				Map<String , Object>properties = node.getAllProperties();
				JSONObject jsonProperties = new JSONObject();
				for(Entry<String , Object> entry : properties.entrySet()){
					jsonProperties.put(entry.getKey() , entry.getValue());
				}
				temp.put("properties" , jsonProperties);
				nodesArray.put(temp);
			}

		}
		//endregion
		for(long edgeID : edges){
			Relationship edge;
			try(Transaction tx = db.beginTx()) {
				edge = db.getRelationshipById(edgeID);
				JSONObject temp = new JSONObject();
				temp.put("id" , edgeID);
				temp.put("type" , edge.getType().toString());
				temp.put("startNode" , edge.getStartNode().getId());
				temp.put("endNode" , edge.getEndNode().getId());
				relationshipsArray.put(temp);
			}

		}

		graph.put("nodes" , nodesArray);
		graph.put("relationships" , relationshipsArray);

		dataArray.put(new JSONObject().put("graph" , graph));

		resultsArray.put(new JSONObject().put("data" , dataArray));

		returnResult.put("results" , resultsArray);
		return returnResult;
	}
}
