package searcher.codepattern;

import de.parsemis.graph.Graph;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import searcher.codepattern.code.mining.Miner;
import searcher.codepattern.code.mining.MiningNode;
import searcher.codepattern.utils.ParseUtil;
import webapp.SnowGraphContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CodePatternSearcher {

    public static List<Graph<MiningNode, Integer>> run(String query, SnowGraphContext context) throws IOException, ParseException {

        List<String> contents=new GithubCodeSearcher(context).search(query, GithubCodeSearcher.RETURN_MODE.CONTENT);

        List<String> methods = contents.stream()
            .map(ParseUtil::parseFileContent)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        methods=rankMethods(methods,query);

        return Miner.mine(methods, Miner.createSetting(4, 3));

    }

    private static List<String> rankMethods(List<String> methods, String query) throws IOException, ParseException {
        List<String> r=new ArrayList<>();
        Directory dir=new RAMDirectory();
        IndexWriter indexWriter=new IndexWriter(dir, new IndexWriterConfig(new EnglishAnalyzer()));
        for (String method:methods){
            Document document=new Document();
            document.add(new TextField("content",method, Field.Store.YES));
            indexWriter.addDocument(document);
        }
        indexWriter.close();
        QueryParser qp = new QueryParser("content", new EnglishAnalyzer());
        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(dir));
        TopDocs topDocs = indexSearcher.search(qp.parse(query), 50);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            r.add(indexSearcher.doc(scoreDoc.doc).get("content"));
        }
        return r;
    }

}
