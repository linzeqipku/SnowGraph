package webapp.resource;

import graphdb.extractors.miners.text.TextExtractor;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import webapp.SnowGraphContext;

import java.util.HashMap;
import java.util.Map;

public class Neo4jNode {

    private final long id;
    private final String label;
    private final Map properties=new HashMap<>();

    public static Neo4jNode get(long id, SnowGraphContext context){
        Neo4jNode node=null;
        Session session = context.getNeo4jBoltDriver().session();
        String stat = "match (n) where id(n)=" + id + " return id(n), labels(n)[0], n";
        StatementResult rs = session.run(stat);
        while (rs.hasNext()) {
            Record item=rs.next();
            node=new Neo4jNode(item.get("id(n)").asLong(),item.get("labels(n)[0]").asString());
            node.properties.putAll(item.get("n").asMap());
            node.properties.put("id",node.id);
            node.properties.put("label",node.label);
        }
        session.close();
        return node;
    }

    private Neo4jNode(long id, String label) {
        this.id = id;
        this.label = label;
    }

    public long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Map getProperties() {
        return properties;
    }
}
