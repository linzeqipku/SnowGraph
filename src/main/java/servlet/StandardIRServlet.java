package servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import searcher.doc.ir.LuceneSearchResult;
import searcher.doc.ir.LuceneSearcher;

public class StandardIRServlet extends HttpServlet {
	
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        String query = request.getParameter("query");
        List<LuceneSearchResult> resultList = new LuceneSearcher().query(query);
        JSONArray results = new JSONArray();
        for (LuceneSearchResult doc : resultList){
        	JSONObject obj = new JSONObject();
        	obj.put("id", doc.id);
        	obj.put("type", doc.type);
        	obj.put("title", doc.title);
        	obj.put("content", doc.content);
        	results.put(obj);
        }
        
        response.getWriter().print(results.toString());
    }

}
