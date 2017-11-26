package searcher;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import graphdb.extractors.miners.text.TextExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import searcher.graph.GraphSearcher;
import searcher.ir.LuceneSearchResult;
import searcher.ir.LuceneSearcher;

public class DocSearcher {

    private String qaExamplePath=null;

    private DocDistScorer docDistScorer = null;
    private GraphSearcher graphSearcher = null;
    private LuceneSearcher keeper = null;
    private Driver connection = null;

    private Map<Long, Long> qaMap = null;
    private Map<Long, String> queryMap = null;

    public DocSearcher(GraphSearcher graphSearcher) {
        this.graphSearcher = graphSearcher;
        this.keeper = new LuceneSearcher();
        this.docDistScorer = new DocDistScorer(graphSearcher);
        connection = graphSearcher.connection;
        extractQaMap();
    }

    public void setQaExamplePath(String path){
        qaExamplePath=path;
    }

    public Pair<String, String> getContent(long nodeId) {
        String plain = "";
        String rich = "";
        Session session = connection.session();
        String stat = "match (n) where id(n)=" + nodeId + " return labels(n)[0], n." + TextExtractor.TITLE + ", n." + TextExtractor.TEXT;
        StatementResult rs = session.run(stat);
        while (rs.hasNext()) {
            Record item = rs.next();
            String label = item.get("labels(n)[0]").asString();
            String text = item.get("n." + TextExtractor.TEXT).asString();
            String title = item.get("n." + TextExtractor.TITLE).asString();
            rich = "<h2>" + title + "</h2>" + text;
            plain = Jsoup.parse("<html>" + rich + "</html>").text();
        }
        session.close();
        return new ImmutablePair<>(plain, rich);
    }

    public String getQuery(long queryId) {
        String title = "";
        Session session = connection.session();
        String stat = "match (n) where id(n)=" + queryId + " return n." + TextExtractor.TITLE;
        StatementResult rs = session.run(stat);
        while (rs.hasNext()) {
            Record item = rs.next();
            title = item.get("n." + TextExtractor.TITLE).asString();
        }
        session.close();
        return title;
    }

    public long getAnswerId(String query){
        for (long qId:queryMap.keySet()){
            if (queryMap.get(qId).trim().equals(query.trim()))
                return qaMap.get(qId);
        }
        return -1;
    }

    public List<DocSearchResult> search(String query) {
        List<DocSearchResult> r = new ArrayList<>();

        Set<Long> graph0 = graphSearcher.query(query).nodes;

        List<LuceneSearchResult> irResultList = keeper.query(query);

        for (int i = 0; i < irResultList.size(); i++) {
            DocSearchResult doc = new DocSearchResult();
            doc.setId(irResultList.get(i).id);
            doc.setIrRank(i + 1);
            doc.setDist(docDistScorer.score(irResultList.get(i).nodeSet, graph0));
            r.add(doc);
        }

        r.sort(Comparator.comparingDouble(DocSearchResult::getDist));

        for (int i = 0; i < r.size(); i++)
            r.get(i).setNewRank(i + 1);

        return r;
    }

    /**
     * 寻找重排序后效果好的StackOverflow问答对作为例子
     */
    public void findExamples() {
        try {
            FileUtils.write(new File(qaExamplePath), "");
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        int count = 0, irCount = 0;
        int qCnt = 0;
        for (long queryId : queryMap.keySet()) {
            qCnt++;
            List<DocSearchResult> list = search(queryMap.get(queryId));
            if (list.size() < 20)
                continue;
            for (int i = 0; i < 20; i++) {
                DocSearchResult current = list.get(i);
                if (current.id == qaMap.get(queryId)) {
                    irCount++;
                    //System.out.println(current.newRank+" "+current.irRank);
                    if (current.newRank < current.irRank) {
                        String res = count + " " + queryId + " " + current.id + " "
                                + current.irRank + "-->" + current.newRank;
                        System.out.println(res + " (" + qCnt + ")");
                        try {
                            FileUtils.write(new File(qaExamplePath), res + "\n", true);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        count++;
                    }
                }
            }
            //System.out.println("query count: " + qCnt + " " + qCnt * 1.0 / qSize * 100 + "%");
        }
        System.out.println("irCount: " + irCount);
    }


    private void extractQaMap() {
        Map<Long, Long> qaMap = new HashMap<>();
        Map<Long, String> qMap = new HashMap<>();
        Session session = connection.session();
        String stat = "match (q:" + StackOverflowExtractor.QUESTION + ")-[:" + StackOverflowExtractor.HAVE_ANSWER + "]->(a:"
                + StackOverflowExtractor.ANSWER + ") where a." + StackOverflowExtractor.ANSWER_ACCEPTED + "=TRUE return id(q),id(a),q."
                + StackOverflowExtractor.QUESTION_TITLE + ", q." + StackOverflowExtractor.QUESTION_BODY;
        StatementResult rs = session.run(stat);
        while (rs.hasNext()) {
            Record item=rs.next();
            long qId = item.get("id(q)").asLong();
            long aId = item.get("id(a)").asLong();
            String query = item.get("q." + StackOverflowExtractor.QUESTION_TITLE).asString();
            qaMap.put(qId, aId);
            qMap.put(qId, query);
        }
        session.close();
        this.qaMap = qaMap;
        this.queryMap = qMap;
    }

}
