package docsearcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.util.test.Test;
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

public class DocSearcher {
	
	GraphDatabaseService graphDB=null;
	DocDistScorer docDistScorer=null;
	GraphSearcher graphSearcher=null;
	
	public DocSearcher(GraphDatabaseService graphDB, GraphSearcher graphSearcher){
		this.graphDB=graphDB;
		this.graphSearcher=graphSearcher;
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
		List<Pair<Long, Set<Long>>> irResultList=null;
		
		for (int i=0;i<irResultList.size();i++){
			DocSearchResult doc=new DocSearchResult();
			doc.setId(irResultList.get(i).getLeft());
			doc.setIrRank(i+1);
			doc.setDist(docDistScorer.score(irResultList.get(i).getRight(), graph0.nodes));
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
		Map<Long, String> queryMap=extractQueries(qaMap);
		int count=0;
		for (long queryId:queryMap.keySet()){
			List<DocSearchResult> list=search(queryMap.get(queryId));
			for (int i=0;i<20;i++){
				if (list.get(i).id==qaMap.get(queryId)){
					if (list.get(i).newRank<list.get(i).irRank){
						System.out.println(count+": "+list.get(i).irRank+"-->"+list.get(i).newRank);
						count++;
					}
					continue;
				}
			}
		}
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

}
