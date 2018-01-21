package searcher.index;

import graphdb.extractors.linkers.apimention.ApiMentionExtractor;
import graphdb.extractors.linkers.ref.ReferenceExtractor;
import graphdb.extractors.miners.codeembedding.line.LINEExtractor;
import graphdb.extractors.parsers.jira.JiraExtractor;
import graphdb.extractors.parsers.mail.MailListExtractor;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.neo4j.driver.v1.Driver;
import searcher.api.ApiLocatorContext;
import searcher.api.SubGraph;
import webapp.SnowGraphContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;

import graphdb.extractors.miners.text.TextExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import searcher.api.ApiLocator;
import utils.TokenizationUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class LuceneSearcher {

    private ApiLocatorContext context = null;
    private QueryParser qp = new QueryParser("content", new EnglishAnalyzer());
    private IndexSearcher indexSearcher = null;
    private String path;

    public LuceneSearcher(ApiLocatorContext context, String dirPath) {
        this.context = context;
        path = dirPath + "/" + "index";
    }

    public void index(boolean overWrite) throws IOException {

        if (!overWrite && new File(path).exists())
            return;

        Directory dir = FSDirectory.open(Paths.get(path));
        Analyzer analyzer = new EnglishAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, iwc);

        Session session = context.connection.session();
        String stat = "match (n) where exists(n." + TextExtractor.IS_TEXT + ") and n." + TextExtractor.IS_TEXT + "=true return id(n), labels(n)[0], n." + TextExtractor.TITLE + ", n." + TextExtractor.TEXT;
        StatementResult rs = session.run(stat);
        int c=0;
        while (rs.hasNext()) {
            Record item = rs.next();
            String label=item.get("labels(n)[0]").asString();
            if (!(label.equals(StackOverflowExtractor.QUESTION)||label.equals(StackOverflowExtractor.ANSWER)||label.equals(JiraExtractor.ISSUE)))
                continue;
            String org_content = item.get("n." + TextExtractor.TEXT).asString();
            String title = item.get("n." + TextExtractor.TITLE).asString();
            String content = dealWithDocument("<html><title>" + title + "</title>" + org_content + "</html>");
            if (content.length() > 0) {
                Document document = new Document();
                long id=item.get("id(n)").asLong();
                document.add(new StringField("id", "" + id, Field.Store.YES));
                document.add(new StringField("type", item.get("labels(n)[0]").asString(), Field.Store.YES));
                document.add(new StringField("title", title, Field.Store.YES));
                document.add(new TextField("content", content, Field.Store.YES));
                document.add(new TextField("org_content", org_content, Field.Store.YES));
                Set<Long> nodes=new HashSet<>();
                Session session1=context.connection.session();
                StatementResult rs1=session1.run("match (a)-[:"+ ApiMentionExtractor.API_NAME_MENTION+"|"+ ReferenceExtractor.REFERENCE+"]->(b) where id(a)="+id+" and exists(b."+ LINEExtractor.LINE_VEC+") return distinct id(b)");
                while (rs1.hasNext()){
                    Record item1=rs1.next();
                    nodes.add(item1.get("id(b)").asLong());
                }
                session1.close();
                String nodeSet = StringUtils.join(nodes, " ").trim();
                document.add(new StringField("node_set", nodeSet, Field.Store.YES));
                writer.addDocument(document);
                System.out.println(c+": "+nodes.size());
                c++;
            }
        }
        session.close();

        writer.close();
    }

    public List<LuceneSearchResult> query(String q){
        return query(q,false);
    }

    public List<LuceneSearchResult> query(String q, boolean onlySoAnswers) {
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
            e.printStackTrace();
            return r;
        }
        TopDocs topDocs = null;
        try {
            if (!onlySoAnswers) {
                topDocs = indexSearcher.search(query, 100);
            } else {
                Query query2=new TermQuery(new Term("type",StackOverflowExtractor.ANSWER));
                BooleanQuery query3=new BooleanQuery.Builder().add(query, BooleanClause.Occur.MUST).add(query2, BooleanClause.Occur.MUST).build();
                topDocs = indexSearcher.search(query3, 100);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document document = null;
            try {
                document = indexSearcher.doc(scoreDoc.doc);
            } catch (IOException e) {
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
            else
                r += " " + token;
        }
        return r.trim();
    }

}
