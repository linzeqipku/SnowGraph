package utils;

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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InMemoryIndex {

    RAMDirectory directory=new RAMDirectory();
    private IndexSearcher indexSearcher = null;
    private QueryParser qp = new QueryParser("content", new EnglishAnalyzer());

    public InMemoryIndex(Map<String,String> id2Text){
        Analyzer analyzer = new EnglishAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        try {
            IndexWriter writer = new IndexWriter(directory, iwc);
            for (String id:id2Text.keySet()) {
                Document doc=new Document();
                doc.add(new StringField("id", id, Field.Store.YES));
                doc.add(new TextField("content", id2Text.get(id), Field.Store.YES));
                writer.addDocument(doc);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> search(String queryString){
        List<String> r=new ArrayList<>();
        if (indexSearcher == null) {
            IndexReader reader = null;
            try {
                reader = DirectoryReader.open(directory);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            indexSearcher = new IndexSearcher(reader);
        }
        if (queryString.trim().length() == 0)
            return r;
        Query query = null;
        try {
            query = qp.parse(queryString);
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
            r.add(document.get("id"));
        }
        return r;
    }

}
