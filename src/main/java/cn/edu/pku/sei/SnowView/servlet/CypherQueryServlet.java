package cn.edu.pku.sei.SnowView.servlet;
import graphsearcher.GraphSearcher;
import graphsearcher.SearchResult;
import org.json.JSONObject;
import org.neo4j.graphdb.GraphDatabaseService;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2017/5/26.
 */
public class CypherQueryServlet extends HttpServlet {
	
	GraphDatabaseService db = null;
	GraphSearcher searcher;
	List <SearchResult> resultCache;
	int resultLength;
	
	public void init(ServletConfig config) throws ServletException{
		//File databasePath = new File("E:\\SnowGraphData\\lucene\\graphdb-lucene-embedding");
        db = GraphDbPool.get("lucene");
        searcher = new GraphSearcher(db);
	}
	
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String query = request.getParameter("query");
        String queryText = request.getParameter("params");
        String requestType = request.getParameter("type");

        
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
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().print(result.toString());

    }
}