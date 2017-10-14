package cn.edu.pku.sei.SnowView.servlet;

import org.apache.commons.io.IOUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Config {

	static private GraphDatabaseService db=null;
	static private String neo4jUrl = null;
	static private String exampleFilePath = null;
	static private String solrUrl = null;
	
	static {
		List<String> lines=new ArrayList<>();
		try {
			lines=IOUtils.readLines(Config.class.getResourceAsStream("/conf"));
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
		return db;
	}
	static public String getUrl(){
		return neo4jUrl;
	}
	static public String getExampleFilePath(){
		return exampleFilePath;
	}
	static public String getSolrUrl(){
		return solrUrl;
	}
}
