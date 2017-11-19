package searcher.graph;

import org.json.JSONArray;
import org.json.JSONObject;
import servlet.Config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
			try (Statement statement = Config.getNeo4jBoltConnection().createStatement()) {
	        	String stat="match (n) where id(n)="+nodeID+" return n";
	        	ResultSet rs=statement.executeQuery(stat);
	        	while (rs.next()){
	        		JSONObject obj=new JSONObject((Map)rs.getObject("n"));
	        		nodesArray.put(obj);
	        	}
	        } catch (SQLException e){
	        	e.printStackTrace();
	        }
		}
		for(long edgeID : edges){System.out.println(edgeID);
			try (Statement statement = Config.getNeo4jBoltConnection().createStatement()) {
	        	String stat="match p=(n)-[r]-(x) where id(r)="+edgeID+" return id(r), id(startNode(r)), id(endNode(r)), type(r)";
	        	ResultSet rs=statement.executeQuery(stat);
	        	while (rs.next()){
	        		JSONObject obj=new JSONObject();
					obj.put("type",rs.getString("type(r)"));
					obj.put("id", rs.getLong("id(r)"));
					obj.put("startNode", rs.getLong("id(startNode(r))"));
					obj.put("endNode", rs.getLong("id(endNode(r))"));
					obj.put("properties",new JSONArray());
					System.out.println(obj);
	        		relationshipsArray.put(obj);
	        	}
	        } catch (SQLException e){
	        	e.printStackTrace();
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
