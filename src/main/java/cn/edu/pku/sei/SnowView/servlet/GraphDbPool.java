package cn.edu.pku.sei.SnowView.servlet;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.mysql.jdbc.ResultSetInternalMethods;

public class GraphDbPool {

	static private Map<String, GraphDatabaseService> dbs=new HashMap<>();
	static private String neo4jUrl = "http://neo4j:1@127.0.0.1:7474";
	static {
		dbs.put("lucene", new GraphDatabaseFactory().newEmbeddedDatabase(new File("E:\\SnowGraphData\\lucene\\graphdb-lucene-embedding")));
	}
	
	static public GraphDatabaseService get(String id){
		return dbs.get(id);
	}
	static public String getUrl(){
		return neo4jUrl;
	}
}
