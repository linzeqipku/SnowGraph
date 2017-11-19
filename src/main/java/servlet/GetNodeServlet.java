package servlet;

import org.json.JSONObject;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Administrator on 2017/5/26.
 */
public class GetNodeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String id = request.getParameter("id");
        Session session = Config.getNeo4jBoltDriver().session();
        String stat = "match (n) where id(n)=" + id + " return n, id(n), labels(n)";
        StatementResult rs = session.run(stat);
        while (rs.hasNext()) {
            Record item=rs.next();
            JSONObject obj = new JSONObject(item.get("n").asMap());
            obj.put("_id",item.get("id(n)").asLong());
            obj.put("_labels",item.get("labels(n)").asList());
            response.getWriter().print(obj);
        }
        session.close();
    }

}
