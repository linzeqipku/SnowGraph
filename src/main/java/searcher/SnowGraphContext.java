package searcher;

import org.apache.commons.io.FileUtils;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import searcher.api.ApiLocatorContext;
import searcher.doc.DocSearcherContext;

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
		System.out.println("SnowGraph context inited.");
	}

	public static DocSearcherContext getDocSearcherContext() {
		return docSearcherContext;
	}

	public static ApiLocatorContext getApiLocatorContext(){
		return apiLocatorContext;
	}

	public static Driver getNeo4jBoltDriver() {
		return getNeo4jBoltDriver();
	}

	public static String getDataPath(){
		return dataPath;
	}

	public static String getGithubAccessToken(){
		return githubAccessToken;
	}

}
