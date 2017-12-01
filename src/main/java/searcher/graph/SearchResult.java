package searcher.graph;

import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import apps.Config;
import servlet.OutGoingRelationServlet;

import java.util.HashSet;
import java.util.Set;

public class SearchResult {

    public Set<Long> nodes = new HashSet<>();
    public Set<Long> edges = new HashSet<>();
    public double cost = 0;
    public double gain = 0;

    public JSONObject toJSON() {
        if (nodes.size() == 0)
            return null;

        JSONArray nodesArray = new JSONArray();
        JSONArray relationshipsArray = new JSONArray();
        JSONObject graph = new JSONObject();

        JSONArray dataArray = new JSONArray();
        JSONArray resultsArray = new JSONArray();
        JSONObject returnResult = new JSONObject();

        for (long nodeID : nodes) {
            Session session = Config.getNeo4jBoltDriver().session();
            String stat = "match (n) where id(n)=" + nodeID + " return n, id(n), labels(n)";
            StatementResult rs = session.run(stat);
            while (rs.hasNext()) {
                Record item=rs.next();
                JSONObject obj = new JSONObject(item.get("n").asMap());
                obj.put("_id",item.get("id(n)").asLong());
                obj.put("_labels",item.get("labels(n)").asList());
                nodesArray.put(obj);
            }
            session.close();
        }
        for (long edgeID : edges) {
            Session session = Config.getNeo4jBoltDriver().session();
            String stat = "match p=(n)-[r]-(x) where id(r)=" + edgeID + " return id(r), id(startNode(r)), id(endNode(r)), type(r)";
            StatementResult rs = session.run(stat);
            while (rs.hasNext()) {
                Record item=rs.next();
                relationshipsArray.put(OutGoingRelationServlet.recordToRel(item));
            }
            session.close();
        }

        graph.put("nodes", nodesArray);
        graph.put("relationships", relationshipsArray);

        dataArray.put(new JSONObject().put("graph", graph));

        resultsArray.put(new JSONObject().put("data", dataArray));

        returnResult.put("results", resultsArray);
        return returnResult;
    }
}
