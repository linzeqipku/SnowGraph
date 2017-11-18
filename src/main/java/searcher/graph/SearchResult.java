package searcher.graph;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import servlet.Config;

import java.io.File;
import java.io.IOException;
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
		for(long edgeID : edges){
			try (Statement statement = Config.getNeo4jBoltConnection().createStatement()) {
	        	String stat="match p=(n)-[r]-(x) where id(r)="+edgeID+" return r";
	        	ResultSet rs=statement.executeQuery(stat);
	        	while (rs.next()){
	        		JSONObject obj=new JSONObject((Map)rs.getObject("r"));
	        		relationshipsArray.put(new JSONObject(obj));
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
