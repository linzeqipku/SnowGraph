package rest.resource;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import searcher.SnowGraphContext;

import java.util.ArrayList;
import java.util.List;

public class Neo4jRelation {

    private final long start,end,id;
    private final String type;

    public static List<Neo4jRelation> getNeo4jRelationList(long nodeId){
        List<Neo4jRelation> list=new ArrayList<>();
        Session session = SnowGraphContext.getNeo4jBoltDriver().session();
        String stat = "match p=(n)-[r]-(x) where id(n)=" + nodeId + " return id(r), id(startNode(r)), id(endNode(r)), type(r)";
        StatementResult rs = session.run(stat);
        while (rs.hasNext()) {
            Record item=rs.next();
            list.add(new Neo4jRelation(item.get("id(startNode(r))").asLong(),item.get("id(endNode(r))").asLong(),item.get("id(r)").asLong(),item.get("type(r)").asString()));
        }
        session.close();
        return list;
    }

    public static Neo4jRelation get(long rId){
        Neo4jRelation r=null;
        Session session = SnowGraphContext.getNeo4jBoltDriver().session();
        String stat = "match p=(n)-[r]-(x) where id(r)=" + rId + " return id(r), id(startNode(r)), id(endNode(r)), type(r)";
        StatementResult rs = session.run(stat);
        while (rs.hasNext()) {
            Record item=rs.next();
            r=new Neo4jRelation(item.get("id(startNode(r))").asLong(),item.get("id(endNode(r))").asLong(),item.get("id(r)").asLong(),item.get("type(r)").asString());
        }
        session.close();
        return r;
    }

    private Neo4jRelation(long start, long end, long id, String type) {
        this.start = start;
        this.end = end;
        this.id = id;
        this.type = type;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
