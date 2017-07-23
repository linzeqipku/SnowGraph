package cn.edu.pku.sei.SnowView.servlet;
import graphdb.extractors.parsers.word.corpus.WordSegmenter;
import graphsearcher.SearchResult;
import org.json.JSONObject;
import org.neo4j.graphdb.GraphDatabaseService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.File;
import java.util.List;

import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import graphsearcher.GraphSearcher;

/**
 * Created by Administrator on 2017/5/26.
 */
public class CypherQueryServlet extends HttpServlet {
	
	GraphDatabaseService db = null;
	GraphSearcher searcher;
	List <SearchResult> resultCache;
	int resultLength;
	
	public void init(ServletConfig config) throws ServletException{
		File databasePath = new File("I:\\data\\graphdb\\graphdb");
        db = new GraphDatabaseFactory().newEmbeddedDatabase(databasePath);
        searcher = new GraphSearcher(db);
	}
	
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        System.out.println("start query");
        String query = request.getParameter("query");
        String queryText = request.getParameter("params");
        String requestType = request.getParameter("type");
       /* type = new String(type.getBytes("GBK") , "GBK");
        System.out.println(type);*/

        
        JSONObject searchResult = new JSONObject();
        int index = 0;
        if(requestType == null){
        	resultCache = searcher.query(queryText);
        	searchResult = resultCache.get(0).toJSON(db);
	        resultLength = resultCache.size();
	        index = 0;
	    }else if(requestType.compareTo("getGraph") == 0){
	    	index = Integer.parseInt(request.getParameter("index"));
	    	SearchResult results = resultCache.get(index);
	    	searchResult = results.toJSON(db);
	    }
        
        JSONObject result = new JSONObject();
        result.put("searchResult" , searchResult);
        result.put("index", index);
        result.put("max" , resultLength);
        System.out.println("end query");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().print(result.toString());


        //region original code
        /*try {
            JSONObject jsobj = new JSONObject();
            JSONObject paraobj = new JSONObject(type);
            jsobj.put("query", query);
            jsobj.put("params", paraobj);
            System.out.println(query);
            System.out.println(paraobj.toString());
            String p = PostUtil.sendPost("http://neo4j:1@127.0.0.1:7474/db/data/cypher", jsobj.toString());
            //String p = PostUtil.sendGet("http://neo4j:123@127.0.0.1:7474/db/data/cypher");
            //System.out.println(p);
            response.setContentType("application/json");
            response.getWriter().print(p);
        }catch(Exception e){
            System.out.println(e.getStackTrace());
        }*/
        //endregion
    }
}