package searcher.graph;

import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.*;

import servlet.Config;
import utils.PostUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;

public class SearchResult {
	
	public Set<Long> nodes=new HashSet<>();
	public Set<Long> edges=new HashSet<>();
	public double cost=0;

	public JSONObject toJSON(){
		if(nodes.size() == 0)
			return null;

		JSONArray nodesArray = new JSONArray();
		JSONArray relationshipsArray = new JSONArray();
		JSONObject graph = new JSONObject();

		JSONArray dataArray = new JSONArray();
		JSONArray resultsArray = new JSONArray();
		JSONObject returnResult = new JSONObject();

		for(long nodeID : nodes){
			String str=PostUtil.sendGet(Config.getNeo4jHttpUrl()+"/db/data/node/"+nodeID);
			JSONObject response = new JSONObject(str);
			JSONObject temp = new JSONObject();
			temp.put("id", nodeID);
			temp.put("labels", response.getJSONObject("metadata").getJSONArray("labels"));
			temp.put("properties" , response.getJSONObject("data"));
			nodesArray.put(temp);
		}
		for(long edgeID : edges){
			String str=PostUtil.sendGet(Config.getNeo4jHttpUrl()+"/db/data/relationship/"+edgeID);
			JSONObject response = new JSONObject(str);
			JSONObject temp = new JSONObject();
			temp.put("id", edgeID);
			temp.put("type", response.getString("type"));
			temp.put("startNode" , Long.parseLong(response.getString("start").substring((Config.getNeo4jHttpUrl()+"/db/data/node/").length()+1)));
			temp.put("endNode" , Long.parseLong(response.getString("end").substring((Config.getNeo4jHttpUrl()+"/db/data/node/").length()+1)));
			relationshipsArray.put(temp);
		}

		graph.put("nodes" , nodesArray);
		graph.put("relationships" , relationshipsArray);

		dataArray.put(new JSONObject().put("graph" , graph));

		resultsArray.put(new JSONObject().put("data" , dataArray));

		returnResult.put("results" , resultsArray);
		return returnResult;
	}
}
