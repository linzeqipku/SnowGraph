package searcher.doc;

import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import searcher.SnowGraphContext;
import searcher.api.ApiLocator;
import searcher.doc.ir.LuceneSearchResult;
import searcher.doc.ir.LuceneSearcher;
import utils.VectorUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class DocSearcher {

    public class DocSearchResult {

        long id;
        int irRank,newRank;
        String title,body;
        double dist;
        boolean highlight;

        public long getId() {
            return id;
        }
        public int getIrRank() {
            return irRank;
        }
        public int getNewRank() {
            return newRank;
        }
        public double getDist() {
            return dist;
        }
        public String getTitle() {
            return title;
        }
        public String getBody() {
            return body;
        }
        public boolean getHighlight() {
            return highlight;
        }

    }

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

        Set<Long> graph0 = ApiLocator.query(query, SnowGraphContext.getApiLocatorContext(), false).getNodes();

        List<LuceneSearchResult> irResultList = luceneSearcher.query(query);

        for (int i = 0; i < irResultList.size(); i++) {
            DocSearchResult doc = new DocSearchResult();
            doc.id=irResultList.get(i).id;
            doc.irRank=i + 1;
            doc.dist=score(irResultList.get(i).nodeSet, graph0);
            r.add(doc);
        }

        r.sort(Comparator.comparingDouble(DocSearchResult::getDist));

        for (int i = 0; i < r.size(); i++) {
            r.get(i).newRank = i + 1;
            Pair<String, String> content=context.getContent(r.get(i).id);
            r.get(i).body=content.getRight();
            r.get(i).title= content.getLeft();
            if (r.get(i).title.length()>100)
                r.get(i).title=r.get(i).title.substring(0,100)+" ...";
            r.get(i).highlight=false;
            if (context.qaMap.containsValue(r.get(i).id)){
                for (long qId:context.qaMap.keySet())
                    if (context.qaMap.get(qId).equals(r.get(i).id)&&context.queryMap.get(qId).trim().equals(query.trim()))
                        r.get(i).highlight=true;
            }
        }

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
