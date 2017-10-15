package cn.edu.pku.sei.SnowView.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
public class Config {

	static private GraphDatabaseService db=null;
	static private String neo4jUrl = null;
	static private String exampleFilePath = null;
	static private String solrUrl = null;
	
	public static void init() {
		List<String> lines=new ArrayList<>();
		try {
			lines=FileUtils.readLines(new File(Config.class.getResource("/").getPath()+"conf2"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line:lines){
			int p=line.indexOf(' ');
			if (p>0){
				String pre=line.substring(0, p);
				String suf=line.substring(p+1);
				if (pre.equals("db"))
					db=new GraphDatabaseFactory().newEmbeddedDatabase(new File(suf));
				if (pre.equals("neo4jUrl"))
					neo4jUrl=suf;
				if (pre.equals("exampleFilePath"))
					exampleFilePath=suf;
				if (pre.equals("solrUrl"))
					solrUrl=suf;
			}
		}
	}
	
	static public GraphDatabaseService getGraphDB(){
		if (db==null)
			init();
		return db;
	}
	static public String getUrl(){
		if (neo4jUrl==null)
			init();
		return neo4jUrl;
	}
	static public String getExampleFilePath(){
		if (exampleFilePath==null)
			init();
		return exampleFilePath;
	}
	static public String getSolrUrl(){
		if (solrUrl==null)
			init();
		return solrUrl;
	}
}
