package solr;

import graphsearcher.GraphSearcher;
import graphsearcher.SearchResult;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by laurence on 17-9-28.
 */
public class SolrKeeper {
    SolrClient client = null;

    public SolrKeeper(String baseUrl){
        client = new HttpSolrClient.Builder(baseUrl).build();
    }

    public void addGraphToIndex(GraphDatabaseService graphDb, GraphSearcher graphSearcher, String coreName){
        DocumentExtractor documentExtractor = new DocumentExtractor(graphDb);
        System.out.println("doc list size: " + documentExtractor.docIdList.size());

        List<SolrInputDocument> documentList = new ArrayList<>();
        for(long id : documentExtractor.docIdList){
            System.out.println("id: " + id);
            String org_content = documentExtractor.getOrgText(graphDb, id);
            String content = Jsoup.parse("<html>" + org_content + "</html>").text();
            SearchResult subGraph = graphSearcher.querySingle(content);
            StringBuilder nBuilder = new StringBuilder();
            for (long nodeId : subGraph.nodes){
                nBuilder.append(nodeId + " ");
            }
            String nodeSet = nBuilder.toString().trim();
            if (content.length() > 0) {
                SolrInputDocument document = new SolrInputDocument();
                document.addField("id", id);
                document.addField("content", content);
                document.addField("org_content", org_content);
                document.addField("node_set", nodeSet);
                documentList.add(document);
            }
            if (documentList.size() >= 600) {
                try {
                    client.add(coreName, documentList);
                    System.out.println("add doc to server");
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

    public List<Pair<Long, Set<Long>>> querySolr(String q, String coreName){
        SolrQuery solrQuery = new SolrQuery();
        /*if (q.length() > 1200){ // uri max length
            System.out.println("exceed max uri length: " + q.length());
            q = q.substring(0, 1200);
        }*/
        String validQ = ClientUtils.escapeQueryChars(q); // 对query中特殊字符转义如：，*，+

        solrQuery.setQuery("content:" + validQ);
        solrQuery.setRows(100);

        List<Pair<Long, Set<Long>>> resPairList = new ArrayList<>();
        try {
            QueryResponse response = client.query(coreName, solrQuery, SolrRequest.METHOD.POST);
            SolrDocumentList docs = response.getResults();
            for (SolrDocument doc : docs){
                Long id = Long.parseLong((String)doc.getFieldValue("id"));
                String subGraph = ((List<String>)doc.getFieldValue("node_set")).get(0);
                Set<Long> nodeSet = new HashSet<>();
                if (subGraph.trim().length() > 0) {
                    for (String node : subGraph.trim().split(" ")) {
                        nodeSet.add(Long.parseLong(node));
                    }
                }
                resPairList.add(new ImmutablePair<>(id, nodeSet));
            }
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resPairList;
    }

    public static void main(String args[]){
        SolrKeeper keeper = new SolrKeeper("http://localhost:8983/solr");
        String path = "E:\\SnowGraphData\\lucene\\graphdb-lucene-embedding";
        GraphDatabaseFactory graphDbFactory = new GraphDatabaseFactory();
        GraphDatabaseService graphDb = graphDbFactory.newEmbeddedDatabase(new File(path));
        GraphSearcher graphSearcher = new GraphSearcher(graphDb);
        keeper.addGraphToIndex(graphDb, graphSearcher, "myCore");
        //keeper.querySolr("solr", "myCore");
    }
}
