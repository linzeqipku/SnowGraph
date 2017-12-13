package searcher.doc.ir;

import searcher.SnowGraphContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;

import graphdb.extractors.miners.text.TextExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import searcher.api.ApiLocator;
import rest.resource.Neo4jSubGraph;
import utils.TokenizationUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class LuceneSearcher {

    private QueryParser qp = new QueryParser("content", new EnglishAnalyzer());
    private IndexSearcher indexSearcher = null;
    private String path=SnowGraphContext.getDataPath()+"/"+"index";

    public void index(boolean test) throws IOException {

        Directory dir = FSDirectory.open(Paths.get(path));
        Analyzer analyzer = new EnglishAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, iwc);

        Session session = SnowGraphContext.getNeo4jBoltDriver().session();
        String stat = "match (n) where exists(n." + TextExtractor.IS_TEXT + ") and n."+TextExtractor.IS_TEXT+"=true return n." + TextExtractor.TITLE + " n." + TextExtractor.TEXT;
        if (test)
            stat = "match (n:" + StackOverflowExtractor.ANSWER + ") where exists(n." + TextExtractor.TEXT + ") and n." + StackOverflowExtractor.ANSWER_ACCEPTED + "=TRUE "
                    + "return n." + TextExtractor.TITLE + ", n." + TextExtractor.TEXT + ", id(n), labels(n)[0]";
        StatementResult rs = session.run(stat);
        while (rs.hasNext()) {
            Record item=rs.next();
            String org_content = item.get("n." + TextExtractor.TEXT).asString();
            String title = item.get("n." + TextExtractor.TITLE).asString();
            String content = dealWithDocument("<html><title>" + title + "</title>" + org_content + "</html>");
            if (content.length() > 0) {
                Document document = new Document();
                document.add(new StringField("id", "" + item.get("id(n)").asLong(), Field.Store.YES));
                document.add(new StringField("type", item.get("labels(n)[0]").asString(), Field.Store.YES));
                document.add(new StringField("title", title, Field.Store.YES));
                document.add(new TextField("content", content, Field.Store.YES));
                document.add(new TextField("org_content", org_content, Field.Store.YES));
                if (test) {
                    ApiLocator.SubGraph subGraph = ApiLocator.query(content,SnowGraphContext.getApiLocatorContext(),false);
                    String nodeSet = StringUtils.join(subGraph.getNodes(), " ").trim();
                    System.out.println(subGraph.getNodes().size());
                    document.add(new StringField("node_set", nodeSet, Field.Store.YES));
                } else
                    document.add(new StringField("node_set", "", Field.Store.YES));
                writer.addDocument(document);
            }
        }
        session.close();

        writer.close();
    }

    public List<LuceneSearchResult> query(String q) {

        List<LuceneSearchResult> r = new ArrayList<>();

        if (indexSearcher == null) {
            IndexReader reader = null;
            try {
                reader = DirectoryReader.open(FSDirectory.open(Paths.get(path)));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            indexSearcher = new IndexSearcher(reader);
        }

        q = dealWithDocument("<html>" + q + "</html>");

        if (q.trim().length() == 0)
            return r;

        Query query = null;
        try {
            query = qp.parse(q);
        } catch (ParseException e) {
            return r;
        }
        TopDocs topDocs = null;
        try {
            topDocs = indexSearcher.search(query, 100);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document document = null;
            try {
                document = indexSearcher.doc(scoreDoc.doc);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            LuceneSearchResult result = new LuceneSearchResult(Long.parseLong(document.get("id")),
                    document.get("type"), document.get("title"), document.get("org_content"),
                    new Double(scoreDoc.score).doubleValue(), document.get("node_set"));
            r.add(result);
        }
        return r;
    }

    private String dealWithDocument(String content) {
        String r = "";
        content = Jsoup.parse(content).text();
        content = content.replaceAll("[^A-Za-z]+", " ");
        for (String token : content.split("\\s+")) {
            List<String> eles = TokenizationUtils.camelSplit(token);
            if (eles.size() > 1)
                r += " " + StringUtils.join(eles, " ");
        }
        return r.trim();
    }

}
