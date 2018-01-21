package searcher.api.expand;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import searcher.api.ApiLocatorContext;
import searcher.api.SubGraph;
import searcher.index.LuceneSearchResult;
import utils.VectorUtils;

import java.util.List;
import java.util.Set;

public class ExpandFlossNodes {

    public static SubGraph run(String query, SubGraph searchResult1, SubGraph linkedSearchResult1,  ApiLocatorContext context){
        SubGraph r = new SubGraph();
        r.getNodes().addAll(searchResult1.getNodes());
        r.cost = searchResult1.cost;
        List<LuceneSearchResult> luceneSearchResults=context.getLuceneSearcher().query(query);
        luceneSearchResults.sort((LuceneSearchResult a, LuceneSearchResult b)->{
            Double aDist=new Double(dist(a.nodeSet,searchResult1.getNodes(),context));
            Double bDist=new Double(dist(b.nodeSet,searchResult1.getNodes(),context));
            return aDist.compareTo(bDist);
        });
        for (int i=0;i<3&&i<luceneSearchResults.size();i++) {
            r.getNodes().add(luceneSearchResults.get(i).id);
            for (long node:linkedSearchResult1.getNodes()){
                Session session=context.connection.session();
                StatementResult rs=session.run("match (a)-[r]-(b) where id(a)="+node+" and id(b)="+luceneSearchResults.get(i).id+" return id(r)");
                while (rs.hasNext()){
                    Record item=rs.next();
                    r.getEdges().add(item.get("id(r)").asLong());
                }
                session.close();
            }
        }
        return r;
    }

    public static double dist(Set<Long> nodeSet1, Set<Long> nodeSet2, ApiLocatorContext context){
        double r=0;
        double c=0;
        for (long id1:nodeSet1){
            if (!context.id2Vec.containsKey(id1))
                continue;
            c++;
            double minDist=Double.MAX_VALUE;
            for (long id2:nodeSet2){
                if (!context.id2Vec.containsKey(id2))
                    continue;
                double dist=dist(id1, id2,context);
                if (dist<minDist)
                    minDist=dist;
            }
            if (minDist!=Double.MAX_VALUE)
                r+=minDist;
            else
                return Double.MAX_VALUE;
        }
        if (c==0)
            return Double.MAX_VALUE;
        return r/c;
    }

    private static double dist(long node1, long node2, ApiLocatorContext context){
        return VectorUtils.dist(node1,node2,context.id2Vec);
    }

}
