package apps;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import searcher.DocSearcher;
import searcher.graph.GraphSearcher;

import java.io.*;
import java.util.*;

public class Config {

	static private String neo4jBoltUrl = null;
	static private Driver neo4jBoltConnection = null;
	static private String exampleFilePath = null;
	static private Set<Long> exampleQuestions = null;
	static private String lucenePath = null;
	static private GraphSearcher graphSearcher = null;
	static private DocSearcher docSearcher = null;
	static private boolean flag = false;

	public static void init() {
		if (flag)
			return;
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
				if (pre.equals("neo4jBoltUrl"))
					neo4jBoltUrl = suf;
				if (pre.equals("dataPath")) {
					String exampleFilePath = suf+"/qaexamples";
					exampleQuestions=getSampleQuestionIds(exampleFilePath);
					lucenePath = suf+"/index";
				}
			}
		}
		neo4jBoltConnection= GraphDatabase.driver(neo4jBoltUrl, AuthTokens.basic("neo4j", "123"));
		graphSearcher = new GraphSearcher(neo4jBoltConnection);
		docSearcher = new DocSearcher(graphSearcher);
		docSearcher.setQaExamplePath(exampleFilePath);
		flag = true;
	}

	private static Set<Long> getSampleQuestionIds(String exampleFilePath){
		Set<Long> r=new HashSet<>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(exampleFilePath)),
					"UTF-8"));
			String lineTxt;
			while ((lineTxt = br.readLine()) != null) {
				if (lineTxt.length()==0)
					continue;
				String[] names = lineTxt.split(" ");
				r.add(Long.parseLong(names[1]));
			}
			br.close();
		} catch (Exception e) {
			System.err.println("read errors :" + e);
		}
		return r;
	}

	static public String getRandomExampleQuery(){
		init();
		long id = new ArrayList<>(exampleQuestions).get(new Random().nextInt(exampleQuestions.size()));
		String query = docSearcher.getQuery(id);
		return query;
	}

	static public String getLucenePath() {
		init();
		return lucenePath;
	}

	static public GraphSearcher getGraphSearcher() {
		init();
		return graphSearcher;
	}

	static public DocSearcher getDocSearcher() {
		init();
		return docSearcher;
	}

	public static Driver getNeo4jBoltDriver() {
		init();
		return neo4jBoltConnection;
	}

}
