package cn.edu.pku.sei.SnowView.servlet;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import searcher.DocSearcher;
import searcher.graph.GraphSearcher;

public class Config {

	static private String neo4jHttpUrl = null;
	static private String neo4jBoltUrl = null;
	static private Connection neo4jBoltConnection = null;
	static private String exampleFilePath = null;
	static private String lucenePath = null;
	static private GraphSearcher graphSearcher = null;
	static private DocSearcher docSearcher = null;
	static private boolean flag = false;

	public static void init() {
		flag = true;
		List<String> lines = new ArrayList<>();
		try {
			lines = FileUtils.readLines(new File(Config.class.getResource("/").getPath() + "conf"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : lines) {
			int p = line.indexOf(' ');
			if (p > 0) {
				String pre = line.substring(0, p);
				String suf = line.substring(p + 1);
				if (pre.equals("neo4jHttpUrl"))
					neo4jHttpUrl = suf;
				if (pre.equals("neo4jBoltUrl"))
					neo4jBoltUrl = suf;
				if (pre.equals("exampleFilePath"))
					exampleFilePath = suf;
				if (pre.equals("lucenePath"))
					lucenePath = suf;
			}
		}
		try {
			neo4jBoltConnection=DriverManager.getConnection("jdbc:neo4j:"+neo4jBoltUrl);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		graphSearcher = new GraphSearcher();
		docSearcher = new DocSearcher(graphSearcher);
	}

	static public String getNeo4jHttpUrl() {
		if (!flag)
			init();
		return neo4jHttpUrl;
	}

	static public String getExampleFilePath() {
		if (!flag)
			init();
		return exampleFilePath;
	}

	static public String getLucenePath() {
		if (!flag)
			init();
		return lucenePath;
	}

	static public GraphSearcher getGraphSearcher() {
		if (!flag)
			init();
		return graphSearcher;
	}

	static public DocSearcher getDocSearcher() {
		if (!flag)
			init();
		return docSearcher;
	}

	public static Connection getNeo4jBoltConnection() {
		if (!flag)
			init();
		return neo4jBoltConnection;
	}

}
