package solr;

import graphdb.extractors.parsers.jira.JiraExtractor;
import graphdb.extractors.parsers.mail.MailListExtractor;
import graphdb.extractors.parsers.stackoverflow.StackOverflowExtractor;
import org.jsoup.Jsoup;
import org.neo4j.graphdb.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by laurence on 17-9-29.
 */
public class DocumentExtractor {
    List<Long> docIdList = new ArrayList<>();

    public DocumentExtractor(GraphDatabaseService graphDb) {
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterator<Node> iterator = graphDb.getAllNodes().iterator();
            while (iterator.hasNext()) {
                Node node = iterator.next();
                if (node.hasLabel(Label.label(StackOverflowExtractor.ANSWER))
                        /*|| node.hasLabel(Label.label(MailListExtractor.MAIL))
                        || node.hasLabel(Label.label(JiraExtractor.ISSUE))*/){
                    docIdList.add(node.getId());
                }
            }
            tx.success();
        }
    }
    public String getOrgText(GraphDatabaseService graphDb, long id){
        String text = "";
        try(Transaction tx = graphDb.beginTx()) {
            Node node = graphDb.getNodeById(id);
            if (node.hasLabel(Label.label(StackOverflowExtractor.ANSWER))) {
                text = (String) node.getProperty(StackOverflowExtractor.ANSWER_BODY);
            } else if (node.hasLabel(Label.label(MailListExtractor.MAIL))) {
                text = (String)node.getProperty(MailListExtractor.MAIL_BODY);
            } else if (node.hasLabel(Label.label(JiraExtractor.ISSUE))) {
                text = (String) node.getProperty(JiraExtractor.ISSUE_DESCRIPTION);
            }
            tx.success();
        }
        return text;
    }
    public String getText(GraphDatabaseService graphDb, long id){
        String orgText = getOrgText(graphDb, id);
        return Jsoup.parse("<html>" + orgText + "</html>").text();
    }
}
