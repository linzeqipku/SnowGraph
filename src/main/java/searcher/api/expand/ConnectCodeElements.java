package searcher.api.expand;

import graphdb.extractors.parsers.javacode.JavaCodeExtractor;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import searcher.api.ApiLocatorContext;
import searcher.api.SubGraph;

import java.util.HashSet;
import java.util.Set;

public class ConnectCodeElements {

    private static final String codeRels = JavaCodeExtractor.EXTEND + "|" + JavaCodeExtractor.IMPLEMENT + "|" + JavaCodeExtractor.THROW + "|"
            + JavaCodeExtractor.PARAM + "|" + JavaCodeExtractor.RT + "|" + JavaCodeExtractor.HAVE_METHOD + "|"
            + JavaCodeExtractor.HAVE_FIELD + "|" + JavaCodeExtractor.CALL_METHOD + "|" + JavaCodeExtractor.CALL_FIELD
            + "|" + JavaCodeExtractor.TYPE + "|" + JavaCodeExtractor.VARIABLE;

    public static SubGraph run(SubGraph searchResult1, ApiLocatorContext context){
        SubGraph r = new SubGraph();
        r.getNodes().addAll(searchResult1.getNodes());
        r.cost = searchResult1.cost;
        Set<Long> flags = new HashSet<>();
        for (long seed1 : searchResult1.getNodes()) {
            if (flags.contains(seed1))
                continue;
            for (long seed2 : searchResult1.getNodes()) {
                if (seed1 == seed2)
                    continue;
                if (flags.contains(seed2))
                    continue;
                Session session = context.connection.session();
                String stat = "match p=shortestPath((n1)-[:" + codeRels + "*..10]-(n2)) where id(n1)=" + seed1 + " and id(n2)=" + seed2
                        + " unwind relationships(p) as r return id(startNode(r)), id(endNode(r)), id(r)";
                StatementResult rs = session.run(stat);
                while (rs.hasNext()) {
                    Record item=rs.next();
                    long node1 = item.get("id(startNode(r))").asLong();
                    long node2 = item.get("id(endNode(r))").asLong();
                    long rel = item.get("id(r)").asLong();
                    r.getNodes().add(node1);
                    r.getNodes().add(node2);
                    r.getEdges().add(rel);
                    flags.add(seed2);
                }
                session.close();
            }
            flags.add(seed1);
        }
        return r;
    }

}
