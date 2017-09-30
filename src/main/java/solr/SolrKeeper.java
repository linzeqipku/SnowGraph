package solr;

import graphdb.extractors.parsers.mail.MailListExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;
import graphsearcher.GraphSearcher;
import graphsearcher.SearchResult;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;
import org.neo4j.cypher.internal.frontend.v2_3.perty.Doc;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by laurence on 17-9-28.
 */
public class SolrKeeper {
    SolrClient client = null;

    public SolrKeeper(String baseUrl){
        client = new HttpSolrClient.Builder(baseUrl).build();
    }

    public void addGraphToIndex(String path, String coreName){
        GraphDatabaseFactory graphDbFactory = new GraphDatabaseFactory();
        GraphDatabaseService graphDb = graphDbFactory.newEmbeddedDatabase(new File(path));

        DocumentExtractor documentExtractor = new DocumentExtractor(graphDb);
        GraphSearcher graphSearcher = new GraphSearcher(graphDb);

        List<SolrInputDocument> documentList = new ArrayList<>();
        for(long id : documentExtractor.docIdList){
            String org_content = documentExtractor.getOrgText(graphDb, id);
            String content = documentExtractor.getText(graphDb, id);
            SearchResult subGraph = graphSearcher.querySingle(content);
            String nodeSet = "";
            for (long nodeId : subGraph.nodes){
                nodeSet += nodeId + " ";
            }
            nodeSet = nodeSet.trim();
            if (content.length() > 0) {
                SolrInputDocument document = new SolrInputDocument();
                document.addField("id", id);
                document.addField("content", content);
                document.addField("org_content", org_content);
                document.addField("node_set", nodeSet);
                documentList.add(document);
            }
            if (documentList.size() >= 500) {
                try {
                    client.add(coreName, documentList);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SolrServerException e) {
                    e.printStackTrace();
                }
                documentList.clear();
            }
        }
        try{
            client.add(coreName, documentList);
        } catch (IOException e){
            e.printStackTrace();
        } catch (SolrServerException e){
            e.printStackTrace();
        }
        graphDb.shutdown();
    }

    public void querySolr(String q, String coreName){
        SolrQuery solrQuery = new SolrQuery(q);
        solrQuery.setRows(100);
        try {
            QueryResponse response = client.query(coreName, solrQuery);
            SolrDocumentList docs = response.getResults();
            for (SolrDocument doc : docs){
                for (String field : doc.getFieldNames()){
                    System.out.println(field + " " + doc.getFieldValue(field));
                }
            }
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]){
        SolrKeeper keeper = new SolrKeeper("192.168.4.244:8983/solr");
        keeper.addGraphToIndex("/media/laurence/TEMP/lucene-primitive", "myCore");
    }
}
