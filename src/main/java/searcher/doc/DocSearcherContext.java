package searcher.doc;

import graphdb.extractors.miners.text.TextExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.tartarus.snowball.ext.EnglishStemmer;
import searcher.api.ApiLocatorContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DocSearcherContext {

    public final Driver connection;
    final Map<Long, List<Double>> id2Vec;
    final Map<Long, Long> qaMap = null;
    final Map<Long, String> queryMap = null;

    public DocSearcherContext(ApiLocatorContext apiLocatorContext) {
        this.connection = apiLocatorContext.connection;
        id2Vec=apiLocatorContext.id2Vec;
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
            queryMap.put(qId, query);
        }
        session.close();
    }

    public Pair<String, String> getContent(long nodeId) {
        String plain = "";
        String rich = "";
        Session session = connection.session();
        String stat = "match (n) where id(n)=" + nodeId + " return labels(n)[0], n." + TextExtractor.TITLE + ", n." + TextExtractor.TEXT;
        StatementResult rs = session.run(stat);
        while (rs.hasNext()) {
            Record item = rs.next();
            String text = item.get("n." + TextExtractor.TEXT).asString();
            String title = item.get("n." + TextExtractor.TITLE).asString();
            rich = "<h2>" + title + "</h2>" + text;
            plain = Jsoup.parse("<html>" + rich + "</html>").text();
        }
        session.close();
        return new ImmutablePair<>(plain, rich);
    }

    public String getQuery(long queryId) {
        return queryMap.get(queryId);
    }

    public long getAnswerId(long queryId) {
        return qaMap.get(queryId);
    }

    public long getAnswerId(String query){
        for (long qId:queryMap.keySet()){
            if (queryMap.get(qId).trim().equals(query.trim()))
                return qaMap.get(qId);
        }
        return -1;
    }

    public Set<Long> getStackOverflowQuestionIds() {
        return queryMap.keySet();
    }
}