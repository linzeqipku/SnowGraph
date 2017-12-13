package rest.resource;

import graphdb.extractors.miners.text.TextExtractor;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import searcher.SnowGraphContext;

public class Neo4jNode {

    private final long id;
    private final String label;
    private final String uniformTitle;
    private final String uniformText;

    public static Neo4jNode get(long id){
        Neo4jNode node=null;
        Session session = SnowGraphContext.getNeo4jBoltDriver().session();
        String stat = "match (n) where id(n)=" + id + " return id(n), labels(n)[0], n."+ TextExtractor.TITLE+", n."+TextExtractor.TEXT;
        StatementResult rs = session.run(stat);
        while (rs.hasNext()) {
            Record item=rs.next();
            String title=item.get("n."+TextExtractor.TITLE).asString();
            String text=item.get("n."+TextExtractor.TEXT).asString();
            node=new Neo4jNode(item.get("id(n)").asLong(),item.get("labels(n)[0]").asString(),title!=null?title:"",text!=null?text:"");
        }
        session.close();
        return node;
    }

    private Neo4jNode(long id, String label, String uniformTitle, String uniformText) {
        this.id = id;
        this.label = label;
        this.uniformTitle = uniformTitle;
        this.uniformText = uniformText;
    }

    public long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getUniformTitle() {
        return uniformTitle;
    }

    public String getUniformText() {
        return uniformText;
    }
}
