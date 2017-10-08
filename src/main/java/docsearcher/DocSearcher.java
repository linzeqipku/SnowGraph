package docsearcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.neo4j.cypher.internal.frontend.v3_1.ast.functions.Has;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;
import graphsearcher.GraphSearcher;
import graphsearcher.SearchResult;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import solr.DocumentExtractor;
import solr.SolrKeeper;

public class DocSearcher {
	
	GraphDatabaseService graphDB=null;
	DocDistScorer docDistScorer=null;
	GraphSearcher graphSearcher=null;
	SolrKeeper keeper=null;
	
	public DocSearcher(GraphDatabaseService graphDB, GraphSearcher graphSearcher, SolrKeeper keeper){
		this.graphDB=graphDB;
		this.graphSearcher=graphSearcher;
		this.keeper = keeper;
		this.docDistScorer=new DocDistScorer(graphSearcher);
	}
	
	public List<DocSearchResult> search(String query){
		List<DocSearchResult> r=new ArrayList<>();
		
		SearchResult graph0=graphSearcher.querySingle(query);

		/*
		 * Todo (lingcy):
		 * irResultList: solr索引返回的前100个结果, {<id,nodes>}
		 *
		 */
		List<Pair<Long, Set<Long>>> irResultList=keeper.querySolr(query, "myCore");;
		
		for (int i=0;i<irResultList.size();i++){
			DocSearchResult doc=new DocSearchResult();
			doc.setId(irResultList.get(i).getKey());
			doc.setIrRank(i+1);
			doc.setDist(docDistScorer.score(irResultList.get(i).getValue(), graph0.nodes));
			r.add(doc);
		}
		
		Collections.sort(r,(r1, r2) -> Double.compare(r1.getDist(), r2.getDist()));
		
		for (int i=0;i<r.size();i++)
			r.get(i).setNewRank(i+1);
		
		return r;
	}
	
	/**
	 * 寻找重排序后效果好的StackOverflow问答对作为例子
	 */
	public void findExamples(){
		Map<Long, Long> qaMap=extractQaMap();
		System.out.println("qaMap size: " + qaMap.size());
		Map<Long, String> queryMap=extractQueries(qaMap);
		System.out.println("query size: " + queryMap.size());
		int count=0, irCount = 0;
		PrintWriter writer = null;
		try{
			writer = new PrintWriter(new FileOutputStream("E:\\Ling\\qaexample"));
		}catch (IOException e){
			e.printStackTrace();
		}
		for (long queryId:queryMap.keySet()){
			List<DocSearchResult> list=search(queryMap.get(queryId));
			for (int i=0;i<20;i++){
				if (list.get(i).id==qaMap.get(queryId)){
					irCount++;
					if (list.get(i).newRank<list.get(i).irRank){
						writer.write(count+": "+list.get(i).irRank+"-->"+list.get(i).newRank + '\n');
						count++;
					}
				}
			}
		}
		System.out.println("irCount: " + irCount);
		writer.close();
	}
	
	/**
	 * 从图数据库中抽取出QA对
	 * @return map: qId-->aId
	 */
	Map<Long, Long> extractQaMap(){
		Map<Long, Long> map=new HashMap<>();
		try (Transaction tx=graphDB.beginTx()){
			ResourceIterator<Node> answers=graphDB.findNodes(Label.label(StackOverflowExtractor.ANSWER));
			while (answers.hasNext()){
				Node answerNode=answers.next();
				if (answerNode.hasProperty(StackOverflowExtractor.ANSWER_ACCEPTED)&&((boolean)answerNode.getProperty(StackOverflowExtractor.ANSWER_ACCEPTED))){
					Node questionNode=answerNode.getRelationships(RelationshipType.withName(StackOverflowExtractor.HAVE_ANSWER),Direction.INCOMING).iterator().next().getStartNode();
					long qId=questionNode.getId();
					long aId=answerNode.getId();
					map.put(qId, aId);
				}
			}
			tx.success();
		}
		return map;
	}
	
	Map<Long, String> extractQueries(Map<Long,Long> qaMap){
		Map<Long, String> rMap=new HashMap<>();
		try (Transaction tx=graphDB.beginTx()){
			for (long id:qaMap.keySet()){
				String query="";
				query+="<html><title>"+graphDB.getNodeById(id).getProperty(StackOverflowExtractor.QUESTION_TITLE)+"</title>";
				query+=" "+graphDB.getNodeById(id).getProperty(StackOverflowExtractor.QUESTION_BODY)+"</html>";
				query=Jsoup.parse(query).text();
				rMap.put(id, query);
			}
			tx.success();
		}
		return rMap;
	}

	public static void main(String[] args){
		String path = "E:\\Ling\\graphdb-lucene-embedding";
		GraphDatabaseFactory graphDbFactory = new GraphDatabaseFactory();
		GraphDatabaseService graphDb = graphDbFactory.newEmbeddedDatabase(new File(path));
		GraphSearcher graphSearcher = new GraphSearcher(graphDb);
		SolrKeeper keeper = new SolrKeeper("http://localhost:8983/solr");
		DocSearcher docSearcher = new DocSearcher(graphDb, graphSearcher, keeper);
		docSearcher.findExamples();
	}
}
