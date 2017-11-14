package cn.edu.pku.sei.SnowView.servlet;

import org.apache.commons.io.IOUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import searcher.DocSearcher;
import searcher.graph.GraphSearcher;
import searcher.ir.LuceneSearcher;

public class Config {

	static private GraphDatabaseService db = null;
	static private String neo4jUrl = null;
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
		String graphPath = null;
		for (String line : lines) {
			int p = line.indexOf(' ');
			if (p > 0) {
				String pre = line.substring(0, p);
				String suf = line.substring(p + 1);
				if (pre.equals("db"))
					graphPath = suf;
				if (pre.equals("neo4jUrl"))
					neo4jUrl = suf;
				if (pre.equals("exampleFilePath"))
					exampleFilePath = suf;
				if (pre.equals("lucenePath"))
					lucenePath = suf;
			}
		}
		db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphPath));
		graphSearcher = new GraphSearcher(db);
		docSearcher = new DocSearcher(db, graphSearcher);
	}

	static public GraphDatabaseService getGraphDB() {
		if (!flag)
			init();
		return db;
	}

	static public String getUrl() {
		if (!flag)
			init();
		return neo4jUrl;
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

}
