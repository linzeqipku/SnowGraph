package webapp;

import org.neo4j.driver.v1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searcher.api.ScoreUtils;
import searcher.index.LuceneSearcher;
import webapp.resource.NavResult;
import searcher.api.ApiLocatorContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SnowGraphContext {

	@Autowired
	public SnowGraphContext(SnowGraphConfig config){
		this.projectPackageName=config.getProjectPackageName();
		this.dataDir=config.getDataDir();
		this.githubAccessToken=config.getGithubAccessToken();
		this.neo4jBoltConnection = GraphDatabase.driver(config.getBoltUrl(), AuthTokens.basic("neo4j", "123"));
		this.apiLocatorContext = new ApiLocatorContext(neo4jBoltConnection,dataDir);
		this.preprocess();
		this.nav = nav();
	}

	private String projectPackageName = null;
	private String dataDir = null;
	private String githubAccessToken = null;
	private Driver neo4jBoltConnection = null;
	private ApiLocatorContext apiLocatorContext=null;
	private NavResult nav = null;

	public ApiLocatorContext getApiLocatorContext(){
		return apiLocatorContext;
	}
	public Driver getNeo4jBoltDriver() {
		return neo4jBoltConnection;
	}
	public NavResult getNav(){
		return nav;
	}
	public String getDataDir(){
		return dataDir;
	}
	public String getGithubAccessToken() {
		return githubAccessToken;
	}
	public String getProjectPackageName() {
		return projectPackageName;
	}

	public void preprocess(){
		ScoreUtils.loadWordVec(this.getDataDir());
		try {
			new LuceneSearcher(apiLocatorContext,dataDir).index(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private NavResult nav(){
		String mainStat="match (a)-[r]->(b) return labels(a)[0]+\" \"+type(r)+\" \"+labels(b)[0] as x, count(*)";

		Session session = neo4jBoltConnection.session();
		String stat = "CALL db.labels() YIELD label";
		StatementResult rs = session.run(stat);
		List<String> labels = new ArrayList<>();
		while (rs.hasNext()) {
			Record item = rs.next();
			labels.add(item.get("label").asString());
		}

		Session session1 = neo4jBoltConnection.session();
		StatementResult rs1=session1.run("match (n) unwind keys(n) as k return count(distinct k)");
		int propertyTypeCount=rs1.next().get("count(distinct k)").asInt();

		Session session2 = neo4jBoltConnection.session();
		StatementResult rs2=session2.run("match (n) unwind keys(n) as k return count(k)");
		int propertyCount=rs2.next().get("count(k)").asInt();

		NavResult r=new NavResult(propertyTypeCount, propertyCount);
		int c = 0;
		for (String label : labels) {
			stat = "match (n:" + label + ") return count(n)";
			int count = 0;
			rs = session.run(stat);
			while (rs.hasNext())
				count = rs.next().get("count(n)").asInt();;
			r.addNode(c,label,count);
			c++;
		}

		c = 0;
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
			r.addRelation(c,src,dst,count,type);
			c++;
		}
		return r;
	}

}
