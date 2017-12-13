package searcher;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.driver.v1.*;
import searcher.api.ApiLocatorContext;
import searcher.doc.DocSearcherContext;
import searcher.doc.example.StackOverflowExamples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SnowGraphContext {

	static private String dataPath = null;
	static private Driver neo4jBoltConnection = null;
	static private ApiLocatorContext apiLocatorContext=null;
	static private DocSearcherContext docSearcherContext = null;
	static private String githubAccessToken = null;
	static private StackOverflowExamples stackOverflowExamples = null;
	static private JSONObject nav = null;

	public static void init() {
		String neo4jBoltUrl ="";
		List<String> lines = new ArrayList<>();
		try {
			lines = FileUtils.readLines(new File(SnowGraphContext.class.getResource("/").getPath() + "conf"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String line : lines) {
			int p = line.indexOf(' ');
			if (p > 0) {
				String pre = line.substring(0, p);
				String suf = line.substring(p + 1);
				if (pre.equals("neo4jBoltUrl"))
					neo4jBoltUrl = suf;
				if (pre.equals("dataPath"))
					dataPath = suf;
				if (pre.equals("github_access_token"))
					githubAccessToken=suf;
			}
		}
		neo4jBoltConnection = GraphDatabase.driver(neo4jBoltUrl, AuthTokens.basic("neo4j", "123"));
		apiLocatorContext = new ApiLocatorContext(neo4jBoltConnection);
		docSearcherContext = new DocSearcherContext(apiLocatorContext);
		stackOverflowExamples=new StackOverflowExamples();
		nav=nav();
		System.out.println("SnowGraph context inited.");
	}

	public static DocSearcherContext getDocSearcherContext() {
		return docSearcherContext;
	}

	public static ApiLocatorContext getApiLocatorContext(){
		return apiLocatorContext;
	}

	public static Driver getNeo4jBoltDriver() {
		return neo4jBoltConnection;
	}

	public static String getDataPath(){
		return dataPath;
	}

	public static String getGithubAccessToken(){
		return githubAccessToken;
	}

	public static StackOverflowExamples getStackOverflowExamples(){
		return stackOverflowExamples;
	}

	private static JSONObject nav(){
		String mainStat="match (a)-[r]->(b) return labels(a)[0]+\" \"+type(r)+\" \"+labels(b)[0] as x, count(*)";

		JSONObject obj = new JSONObject();
		Session session = SnowGraphContext.getNeo4jBoltDriver().session();
		String stat = "CALL db.labels() YIELD label";
		StatementResult rs = session.run(stat);
		List<String> labels = new ArrayList<>();
		while (rs.hasNext()) {
			Record item = rs.next();
			labels.add(item.get("label").asString());
		}

		JSONArray nodeArray = new JSONArray();
		int c = 0;
		for (String label : labels) {
			stat = "match (n:" + label + ") return count(n)";
			int count = 0;
			rs = session.run(stat);
			while (rs.hasNext())
				count = rs.next().get("count(n)").asInt();
			JSONObject nodeObj = new JSONObject();
			nodeObj.put("id", c);
			nodeObj.put("label", label);
			nodeObj.put("count", count);
			nodeArray.put(nodeObj);
			c++;
		}
		obj.put("nodes", nodeArray);

		c = 0;
		JSONArray relArray = new JSONArray();
		stat = mainStat;
		rs = session.run(stat);
		while (rs.hasNext()) {
			Record item = rs.next();
			String x = item.get("x").asString();
			int count = item.get("count(*)").asInt();
			String[] eles = x.split("\\s+");
			int src = labels.indexOf(eles[0]);
			String type = eles[1];
			int dst = labels.indexOf(eles[2]);
			JSONObject relObj = new JSONObject();
			relObj.put("id", c);
			relObj.put("type", type);
			relObj.put("startNode", src);
			relObj.put("endNode", dst);
			relObj.put("count", count);
			relArray.put(relObj);
			c++;
		}
		obj.put("relationships", relArray);
		return obj;
	}

	public static JSONObject getNav(){
		return nav;
	}

}
