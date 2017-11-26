package servlet;

import apps.Config;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import searcher.DocSearchResult;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2017/5/26.
 */
public class RankServlet extends HttpServlet {

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
        List<DocSearchResult> resultList = Config.getDocSearcher().search(query);
        JSONObject searchResult = new JSONObject();
        JSONArray results = new JSONArray();
        for (DocSearchResult doc : resultList) {
            JSONObject obj = new JSONObject();
            Pair<String, String> pair = Config.getDocSearcher().getContent(doc.getId());
            if (pair.getLeft().length() > 110)
                obj.put("title", pair.getLeft().substring(0, 100) + "......");
            else
                obj.put("title", pair.getLeft());
            obj.put("body", pair.getRight());
            obj.put("finalRank", doc.getNewRank());
            obj.put("solrRank", doc.getIrRank());
            obj.put("highlight", Config.getDocSearcher().getAnswerId(query)==doc.getId());
            results.put(obj);
        }
        searchResult.put("query", query);
        searchResult.put("rankedResults", results);
        searchResult.put("solrResults", new JSONArray());

        response.getWriter().print(searchResult.toString());
    }

}