package servlet;

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
import java.sql.ResultSet;
import java.sql.SQLException;
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

        List<JSONObject> list = new ArrayList<>();
        Map<String, Integer> cnt = new HashMap<>();

        Session session = Config.getNeo4jBoltDriver().session();
        String stat = "match p=(n)-[r]-(x) where id(n)=" + id + " return id(r), id(startNode(r)), id(endNode(r)), type(r)";
        StatementResult rs = session.run(stat);
        while (rs.hasNext()) {
            Record item=rs.next();
            JSONObject jsobj = new JSONObject();
            jsobj.put("type", item.get("type(r)").asString());
            jsobj.put("id", item.get("id(r)").asLong());
            jsobj.put("startNode", item.get("id(startNode(r))").asLong());
            jsobj.put("endNode", item.get("id(endNode(r))").asLong());
            jsobj.put("properties", new JSONArray());
            list.add(jsobj);
            String key = jsobj.getString("type");
            if (cnt.containsKey(key)) {
                cnt.put(key, cnt.get(key) + 1);
            } else cnt.put(key, 1);
        }
        session.close();

        Object[] tmp = cnt.keySet().toArray();
        for (int i = 0; i < tmp.length; i++) {
            for (int j = i + 1; j < tmp.length; j++) {
                int t1 = cnt.get(tmp[i].toString());
                int t2 = cnt.get(tmp[j].toString());
                if (t2 < t1) {
                    Object tt = tmp[i];
                    tmp[i] = tmp[j];
                    tmp[j] = tt;
                }
            }
        }
        JSONArray rejsarr = new JSONArray();
        for (Object key : tmp) {
            String k = (String) key;
            for (JSONObject obj : list) {
                if (obj.getString("type").equals(k)) {
                    String flag = "in_";
                    if (obj.getLong("startNode") == Long.parseLong(id)) flag = "ou_";
                    if (flag.equals("in_")) continue;
                    obj.put("type", flag + k);
                    rejsarr.put(obj);
                }
            }
            for (JSONObject obj : list) {
                if (obj.getString("type").equals(k)) {
                    String flag = "in_";
                    if (obj.getLong("startNode") == Long.parseLong(id)) flag = "ou_";
                    if (flag.equals("ou_")) continue;
                    obj.put("type", flag + k);
                    rejsarr.put(obj);
                }
            }
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(rejsarr.toString());
    }

}
