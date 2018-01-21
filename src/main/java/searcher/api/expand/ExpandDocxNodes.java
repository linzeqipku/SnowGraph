package searcher.api.expand;

import graphdb.extractors.linkers.codeindoc_ch.CodeInDocxFileExtractor;
import graphdb.extractors.linkers.designtorequire_ch.DesignToRequireExtractor;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import searcher.api.ApiLocatorContext;
import searcher.api.SubGraph;
import searcher.index.LuceneSearchResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExpandDocxNodes {

    public static SubGraph run(SubGraph searchResult1, ApiLocatorContext context){
        SubGraph r = new SubGraph();
        r.getNodes().addAll(searchResult1.getNodes());
        r.cost = searchResult1.cost;
        for (long node : searchResult1.getNodes()) {
            Session session = context.connection.session();
            String stat = "match p=(n1)-[:" + CodeInDocxFileExtractor.API_EXPLAINED_BY + "]->(n2) where id(n1)=" + node + " unwind relationships(p) as r return id(r), id(startNode(r)), id(endNode(r))";
            StatementResult rs = session.run(stat);
            while (rs.hasNext()) {
                Record item=rs.next();
                long node1 = item.get("id(startNode(r))").asLong();
                long node2 = item.get("id(endNode(r))").asLong();
                long rel = item.get("id(r)").asLong();
                r.getNodes().add(node1);
                r.getNodes().add(node2);
                r.getEdges().add(rel);
            }
            session.close();
        }
        return r;
    }

}
