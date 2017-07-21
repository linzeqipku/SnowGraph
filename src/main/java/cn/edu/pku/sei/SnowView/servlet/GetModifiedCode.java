package cn.edu.pku.sei.SnowView.servlet;

import cn.edu.pku.sei.SnowView.utils.PostUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/5/26.
 */
public class GetModifiedCode extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<JSONObject> nodes = new ArrayList<>();
        List<JSONObject> relationships = new ArrayList<>();
        Set<String> nodeset = new HashSet<>();
        Set<String> relset = new HashSet<>();
        String id = request.getParameter("id");
        nodeset.add(id);
        String p = PostUtil.sendGet("http://neo4j:1@127.0.0.1:7474/db/data/node/"+id);
        System.out.println(p);
        JSONObject jsobj = new JSONObject(p);
        nodes.add(jsobj);


        JSONObject postobj = new JSONObject();
        postobj.put("query","MATCH p=(n:Class)-[r:commit_change_the_class]-(m:gitCommit)-[r1:person_is_author_of_commit]-(k:gitCommitAuthor) where n.name='QueryParser' RETURN m");
        postobj.put("params",new JSONObject());

        JSONArray nodesarr = new JSONArray(nodes);
        JSONArray relsarr = new JSONArray(relationships);
        jsobj = new JSONObject();
        jsobj.put("nodes",nodesarr);
        jsobj.put("relationships",relsarr);
        //System.out.println(jsobj.toString());
        response.setContentType("application/json");
        response.getWriter().print(jsobj.toString());
    }

}
