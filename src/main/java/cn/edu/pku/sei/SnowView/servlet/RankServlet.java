package cn.edu.pku.sei.SnowView.servlet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import docsearcher.DocDistScorer;
import docsearcher.DocSearchResult;
import docsearcher.DocSearcher;
import graphsearcher.GraphSearcher;
import graphsearcher.SearchResult;
import solr.SolrKeeper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Administrator on 2017/5/26.
 */
public class RankServlet extends HttpServlet {
	DocSearcher docSearcher;
	Random rand ;
	Map<Integer, Pair<Integer,Integer>> map = new HashMap<Integer, Pair<Integer,Integer>>();
	public void init(ServletConfig config) throws ServletException{
		GraphDatabaseService graphDb = Config.getGraphDB();
		GraphSearcher graphSearcher = new GraphSearcher(graphDb);
		SolrKeeper keeper = new SolrKeeper(Config.getSolrUrl());
		docSearcher = new DocSearcher(graphDb, graphSearcher, keeper);
		rand = new Random();

        /* 读取数据 */
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(Config.getExampleFilePath())),
                                                                         "UTF-8"));
            String lineTxt = null;
            while ((lineTxt = br.readLine()) != null) {
                String[] names = lineTxt.split(" ");
                map.put(Integer.parseInt(names[0]), new ImmutablePair<Integer,Integer>(Integer.parseInt(names[1]), Integer.parseInt(names[2])));
            }
            br.close();
        } catch (Exception e) {
            System.err.println("read errors :" + e);
        }		
	}
	
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        long id = rand.nextInt(map.size());
        String query = docSearcher.getContent(map.get((int)id).getLeft()).getLeft();
        String query2 = docSearcher.getContent(map.get((int)id).getLeft()).getRight();
        List<DocSearchResult> resultList = docSearcher.search(query);
        JSONObject searchResult = new JSONObject();
        JSONArray results = new JSONArray();
        for (DocSearchResult doc : resultList){
        	JSONObject obj = new JSONObject();
        	obj.put("answerId", doc.getId());
        	Pair<String, String> pair = docSearcher.getContent(doc.getId());
        	if (pair.getLeft().length() > 110) obj.put("title", pair.getLeft().substring(0, 100) + "......"); else
        		obj.put("title", pair.getLeft());
        	obj.put("body", pair.getRight());
        	obj.put("finalRank", doc.getNewRank());
        	obj.put("solrRank", doc.getIrRank());
        	obj.put("relevance", 0);
        	results.put(obj);
        }
        searchResult.put("query", query);
        searchResult.put("query2", query2);
        searchResult.put("answerId", map.get((int)id).getRight());
        searchResult.put("rankedResults", results);
        searchResult.put("solrResults", new JSONArray());
        
        response.getWriter().print(searchResult.toString());
    }

}