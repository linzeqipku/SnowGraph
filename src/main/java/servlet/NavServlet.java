package servlet;

import searcher.SnowGraphContext;
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
import java.util.ArrayList;
import java.util.List;

public class NavServlet extends HttpServlet {

    static JSONObject navObj=null;

    @Override
    public void init() throws ServletException{
        SnowGraphContext.init();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONObject nav = doNav();

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().print(nav.toString());

    }

    private JSONObject doNav() {

        if (navObj!=null)
            return navObj;

        String mainStat="match (a)-[r]->(b) return labels(a)[0]+\" \"+type(r)+\" \"+labels(b)[0] as x, count(*)";

        JSONObject obj = new JSONObject();
        Session session = SnowGraphContext.getNeo4jBoltDriver().session();
        String stat = "CALL db.labels() YIELD label";
        StatementResult rs = session.run(stat);
        List<String> labels = new ArrayList<>();
        while (rs.hasNext()) {
            Record item = rs.next();
            labels.add(item.get("label").asString());
        }

        JSONArray nodeArray = new JSONArray();
        int c = 0;
        for (String label : labels) {
            stat = "match (n:" + label + ") return count(n)";
            int count = 0;
            rs = session.run(stat);
            while (rs.hasNext())
                count = rs.next().get("count(n)").asInt();
            JSONObject nodeObj = new JSONObject();
            nodeObj.put("id", c);
            nodeObj.put("label", label);
            nodeObj.put("count", count);
            nodeArray.put(nodeObj);
            c++;
        }
        obj.put("nodes", nodeArray);

        c = 0;
        JSONArray relArray = new JSONArray();
        stat = mainStat;
        rs = session.run(stat);
        while (rs.hasNext()) {
            Record item = rs.next();
            String x = item.get("x").asString();
            int count = item.get("count(*)").asInt();
            String[] eles = x.split("\\s+");
            int src = labels.indexOf(eles[0]);
            String type = eles[1];
            int dst = labels.indexOf(eles[2]);
            JSONObject relObj = new JSONObject();
            relObj.put("id", c);
            relObj.put("type", type);
            relObj.put("startNode", src);
            relObj.put("endNode", dst);
            relObj.put("count", count);
            relArray.put(relObj);
            c++;
        }
        obj.put("relationships", relArray);

        navObj=obj;
        return obj;
    }

    public static void main(String[] args) {
        System.out.println(new NavServlet().doNav().toString());
    }

}
