package servlet;

import apps.Config;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by Administrator on 2017/5/26.
 */
public class OutGoingRelationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String id = request.getParameter("id");
        //System.out.println("OutGoing: "+id);

        JSONArray rels=new JSONArray();

        Session session = Config.getNeo4jBoltDriver().session();
        String stat = "match p=(n)-[r]-(x) where id(n)=" + id + " return id(r), id(startNode(r)), id(endNode(r)), type(r)";
        StatementResult rs = session.run(stat);
        while (rs.hasNext()) {
            Record item=rs.next();
            rels.put(recordToRel(item));
        }
        session.close();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(rels.toString());
    }

    public static JSONObject recordToRel(Record item){
        JSONObject jsobj = new JSONObject();
        jsobj.put("type", item.get("type(r)").asString());
        jsobj.put("id", item.get("id(r)").asLong());
        jsobj.put("startNode", item.get("id(startNode(r))").asLong());
        jsobj.put("endNode", item.get("id(endNode(r))").asLong());
        jsobj.put("properties", new JSONArray());
        return jsobj;
    }

}
