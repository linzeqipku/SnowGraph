package cn.edu.pku.sei.SnowView.servlet;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class GraphDbPool {

	static private Map<String, GraphDatabaseService> dbs=new HashMap<>();
	
	static {
		dbs.put("lucene", new GraphDatabaseFactory().newEmbeddedDatabase(new File("E:\\SnowGraphData\\lucene\\graphdb-lucene-embedding")));
	}
	
	static public GraphDatabaseService get(String id){
		return dbs.get(id);
	}
	
}
