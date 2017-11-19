package searcher;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import graphdb.extractors.miners.text.TextExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;
import searcher.graph.GraphSearcher;
import searcher.ir.LuceneSearchResult;
import searcher.ir.LuceneSearcher;
import servlet.Config;

public class DocSearcher {

	DocDistScorer docDistScorer=null;
	GraphSearcher graphSearcher=null;
	LuceneSearcher keeper=null;
	Connection connection = null;
	
	Map<Long, Long> qaMap=null;
	Map<Long, String> queryMap=null;
	
	public DocSearcher(GraphSearcher graphSearcher){
		this.graphSearcher=graphSearcher;
		this.keeper = new LuceneSearcher();
		this.docDistScorer=new DocDistScorer(graphSearcher);
		connection = Config.getNeo4jBoltConnection();
	}

	public Pair<String,String> getContent(long nodeId){
		String plain="";
		String rich="";
		try (Statement statement = connection.createStatement()) {
			String stat="match (n) where id(n)="+nodeId+" return labels(n)[0], n."+TextExtractor.TITLE+", n."+TextExtractor.TEXT;
			ResultSet rs=statement.executeQuery(stat);
			while (rs.next()){
				String label=rs.getString("labels(n)[0]");
				String text=rs.getString("n."+TextExtractor.TEXT);
				String title=rs.getString("n."+TextExtractor.TITLE);
				rich="<h2>"+title+"</h2>"+text;
				plain=Jsoup.parse("<html>"+rich+"</html>").text();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new ImmutablePair<>(plain, rich);
	}

	public String getQuery(long queryId){
		return queryMap.get(queryId);
	}

	public List<DocSearchResult> search(String query){
		List<DocSearchResult> r=new ArrayList<>();

		Set<Long> graph0=graphSearcher.query(query).nodes;

		List<LuceneSearchResult> irResultList=keeper.query(query);

		for (int i=0;i<irResultList.size();i++){
			DocSearchResult doc=new DocSearchResult();
			doc.setId(irResultList.get(i).id);
			doc.setIrRank(i+1);
			doc.setDist(docDistScorer.score(irResultList.get(i).nodeSet, graph0));
			r.add(doc);
		}

		r.sort(Comparator.comparingDouble(DocSearchResult::getDist));

		for (int i=0;i<r.size();i++)
			r.get(i).setNewRank(i+1);

		return r;
	}

	/**
	 * 寻找重排序后效果好的StackOverflow问答对作为例子
	 */
	public void findExamples(){
		try {
			FileUtils.write(new File(Config.getExampleFilePath()), "");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		extractQaMap();
		int count=0, irCount = 0;
		int qCnt = 0;
		for (long queryId:queryMap.keySet()){
			qCnt++;
			List<DocSearchResult> list=search(queryMap.get(queryId));
			if (list.size()<20)
				continue;
			for (int i=0;i<20;i++){
			    DocSearchResult current = list.get(i);
				if (current.id == qaMap.get(queryId)){
					irCount++;
					//System.out.println(current.newRank+" "+current.irRank);
					if (current.newRank < current.irRank){
					    String res = count+" " +queryId + " " + current.id + " "
                                + current.irRank+"-->"+current.newRank;
						System.out.println(res+" ("+qCnt+")");
					    try {
							FileUtils.write(new File(Config.getExampleFilePath()), res+"\n", true);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						count++;
					}
				}
			}
			//System.out.println("query count: " + qCnt + " " + qCnt * 1.0 / qSize * 100 + "%");
		}
		System.out.println("irCount: " + irCount);
	}


	void extractQaMap(){
		Map<Long, Long> qaMap=new HashMap<>();
		Map<Long, String> qMap=new HashMap<>();
		try (Statement statement = connection.createStatement()) {
			String stat="match (q:"+StackOverflowExtractor.QUESTION+")-[:"+StackOverflowExtractor.HAVE_ANSWER+"]->(a:"
					+StackOverflowExtractor.ANSWER+") where a."+StackOverflowExtractor.ANSWER_ACCEPTED+"=TRUE return id(q),id(a),q."
					+StackOverflowExtractor.QUESTION_TITLE+", q."+StackOverflowExtractor.QUESTION_BODY;
			ResultSet rs=statement.executeQuery(stat);
			while (rs.next()){
				long qId=rs.getLong("id(q)");
				long aId=rs.getLong("id(a)");
				String query=rs.getString("q."+StackOverflowExtractor.QUESTION_TITLE);
				qaMap.put(qId, aId);
				qMap.put(qId, query);
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
		this.qaMap=qaMap;
		this.queryMap=qMap;
	}

	public static void main(String[] args){
		DocSearcher docSearcher = Config.getDocSearcher();
		docSearcher.findExamples();
	}
}
