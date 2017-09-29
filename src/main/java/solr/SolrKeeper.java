package solr;

import graphdb.extractors.parsers.mail.MailListExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by laurence on 17-9-28.
 */
public class SolrKeeper {
    SolrClient client = null;
    public SolrKeeper(String baseUrl){
        client = new HttpSolrClient.Builder(baseUrl).build();
    }

    public void addGraphData(String path, String coreName){
        GraphDatabaseFactory graphDbFactory = new GraphDatabaseFactory();
        GraphDatabaseService graphDb = graphDbFactory.newEmbeddedDatabase(new File(path));
        int stackCnt = 0, mailCnt = 0;
        try(Transaction tx = graphDb.beginTx()){
            ResourceIterator<Node> iterator = graphDb.getAllNodes().iterator();
            while(iterator.hasNext()){
                Node node = iterator.next();
                long id = -1;
                String type = "", content = "";
                if (node.hasLabel(Label.label(StackOverflowExtractor.ANSWER))){
                    stackCnt += 1;
                    id = node.getId();
                    type = StackOverflowExtractor.ANSWER;
                    content = (String)node.getProperty(StackOverflowExtractor.ANSWER_BODY);
                    content = Jsoup.parse("<html>" + content + "</html>").text();
                    if (id % 37 == 0)
                        System.out.println(content);
                }
                else if (node.hasLabel(Label.label(MailListExtractor.MAIL))){
                    mailCnt += 1;
                    id = node.getId();
                    type = MailListExtractor.MAIL;
                    content = (String)node.getProperty(MailListExtractor.MAIL_SUBJECT)
                        + "\n" + (String)node.getProperty(MailListExtractor.MAIL_BODY);
                }
                if (id >= 0 && content.length() > 0) {
                    SolrInputDocument document = new SolrInputDocument();
                    document.addField("id", id);
                    document.addField("type", type);
                    document.addField("content", content);
                    try{
                        client.add(coreName, document);
                    } catch (IOException e){
                        e.printStackTrace();
                    } catch (SolrServerException e){
                        e.printStackTrace();
                    }
                }
            }
            tx.success();
        }
        System.out.println("mail: " + mailCnt + " stack: " + stackCnt);
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
        SolrKeeper keeper = new SolrKeeper("localhost:8983/solr");
        keeper.addGraphData("/media/laurence/TEMP/lucene-primitive", "myCore");
    }
}
