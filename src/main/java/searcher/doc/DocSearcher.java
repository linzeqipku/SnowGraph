package searcher.doc;

import searcher.SnowGraphContext;
import graphdb.extractors.miners.text.TextExtractor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import searcher.api.ApiLocator;
import searcher.ir.LuceneSearchResult;
import searcher.ir.LuceneSearcher;
import utils.parse.VectorUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class DocSearcher {

    private LuceneSearcher luceneSearcher = null;
    private DocSearcherContext context;

    private DocSearcher(DocSearcherContext context) {
        this.context=context;
        this.luceneSearcher = new LuceneSearcher();
    }

    public static List<DocSearchResult> search(String query, DocSearcherContext context){
        return new DocSearcher(context).search(query);
    }

    private List<DocSearchResult> search(String query) {
        List<DocSearchResult> r = new ArrayList<>();

        Set<Long> graph0 = ApiLocator.query(query, SnowGraphContext.getApiLocatorContext(), false).nodes;

        List<LuceneSearchResult> irResultList = luceneSearcher.query(query);

        for (int i = 0; i < irResultList.size(); i++) {
            DocSearchResult doc = new DocSearchResult();
            doc.setId(irResultList.get(i).id);
            doc.setIrRank(i + 1);
            doc.setDist(score(irResultList.get(i).nodeSet, graph0));
            r.add(doc);
        }

        r.sort(Comparator.comparingDouble(DocSearchResult::getDist));

        for (int i = 0; i < r.size(); i++)
            r.get(i).setNewRank(i + 1);

        return r;
    }

    private double score(Set<Long> nodeSet1, Set<Long> nodeSet2){
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
                double dist=dist(id1, id2);
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

    private double dist(long node1, long node2){
        return VectorUtils.dist(node1,node2,context.id2Vec);
    }

}
