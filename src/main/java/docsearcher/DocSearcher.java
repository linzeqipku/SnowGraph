package docsearcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
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
import ir.LuceneSearchResult;
import ir.LuceneSearcher;

import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import cn.edu.pku.sei.SnowView.servlet.Config;

public class DocSearcher {
	
	GraphDatabaseService graphDB=null;
	DocDistScorer docDistScorer=null;
	GraphSearcher graphSearcher=null;
	LuceneSearcher keeper=null;
	
	public DocSearcher(GraphDatabaseService graphDB, GraphSearcher graphSearcher){
		this.graphDB=graphDB;
		this.graphSearcher=graphSearcher;
		this.keeper = new LuceneSearcher();
		this.docDistScorer=new DocDistScorer(graphSearcher);
	}
	
	/**
	 * 
	 * @param nodeId
	 * @return {<plain-text,rich-text>}
	 */
	public Pair<String,String> getContent(long nodeId){
		String plain="";
		String rich="";
		try (Transaction tx=graphDB.beginTx()){
			
			if (graphDB.getNodeById(nodeId).hasLabel(Label.label(StackOverflowExtractor.QUESTION))){
				rich+="<h2>"+graphDB.getNodeById(nodeId).getProperty(StackOverflowExtractor.QUESTION_TITLE)+"</h2>";
				rich+=" "+graphDB.getNodeById(nodeId).getProperty(StackOverflowExtractor.QUESTION_BODY);
				plain+=Jsoup.parse("<html>"+rich+"</html>").text();
			}
			else if (graphDB.getNodeById(nodeId).hasLabel(Label.label(StackOverflowExtractor.ANSWER))){
				rich+=graphDB.getNodeById(nodeId).getProperty(StackOverflowExtractor.ANSWER_BODY);
				plain+=Jsoup.parse("<html>"+rich+"</html>").text();
			}
			
			tx.success();
		}
		return new ImmutablePair<String,String>(plain, rich);
	}
	
	public List<DocSearchResult> search(String query){
		List<DocSearchResult> r=new ArrayList<>();
		
		Set<Long> graph0=graphSearcher.query(query).nodes;

		/*
		 * Todo (lingcy):
		 * irResultList: solr索引返回的前100个结果, {<id,nodes>}
		 *
		 */
		List<LuceneSearchResult> irResultList=keeper.query(query);;
		
		for (int i=0;i<irResultList.size();i++){
			DocSearchResult doc=new DocSearchResult();
			doc.setId(irResultList.get(i).id);
			doc.setIrRank(i+1);
			doc.setDist(docDistScorer.score(irResultList.get(i).nodeSet, graph0));
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
		try {
			FileUtils.write(new File(Config.getExampleFilePath()), "");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Map<Long, Long> qaMap=extractQaMap();
		System.out.println("qaMap size: " + qaMap.size());
		Map<Long, String> queryMap=extractQueries(qaMap);
		System.out.println("query size: " + queryMap.size());
		int count=0, irCount = 0;
		int qCnt = 0;
		for (long queryId:queryMap.keySet()){
			qCnt++;
			List<DocSearchResult> list=search(queryMap.get(queryId));
			for (int i=0;i<20;i++){
			    DocSearchResult current = list.get(i);
				if (current.id == qaMap.get(queryId)){
					irCount++;
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
		DocSearcher docSearcher = Config.getDocSearcher();
		docSearcher.findExamples();
	}
}
