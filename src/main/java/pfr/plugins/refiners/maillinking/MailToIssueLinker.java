package pfr.plugins.refiners.maillinking;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neo4j.graphdb.*;

import pfr.PFR;
import pfr.annotations.PropertyDeclaration;
import pfr.annotations.RelationDeclaration;
import pfr.plugins.parsers.issuetracker.PfrPluginForIssueTracker;
import pfr.plugins.parsers.mail.PfrPluginForMailList;

/**
 * Created by laurence on 16-11-27.
 */
public class MailToIssueLinker implements PFR{
    @RelationDeclaration
    public static final String MAIL_TO_ISSUE = "mailToIssue";

    GraphDatabaseService db = null;
    HashMap<String, Node> mailMap = new HashMap<>();
    HashMap<String, Node> nameToIssueMap = new HashMap<>();
    HashMap<Node, ArrayList<Node>> mailToIssueMap = new HashMap<>();
    @Override
    public void run(GraphDatabaseService graphDB) {
        this.db = graphDB;
        getMailAndIssueMap();
        extractIssueLink();
    }
    public void extractIssueLink() {
        for (String content : mailMap.keySet()) {
            String patternForLucene = "https?://issues\\.apache\\.org(.*?)/(LUCENE-(\\d+))";
            Pattern regex = Pattern.compile(patternForLucene);
            Matcher idMatcher = regex.matcher(content);
            HashSet<String> issueNameSet = new HashSet<>();
            while (idMatcher.find()) {
                String issueName = idMatcher.group(2);
                issueNameSet.add(issueName);
            }
            ArrayList<Node> issueList = new ArrayList<>();
            for (String name : issueNameSet) {
                Node node = nameToIssueMap.get(name);
                if (node != null) {
                    issueList.add(node);
                    //System.out.println("found in issue map");
                }
            }
            mailToIssueMap.put(mailMap.get(content), issueList);
        }
        try (Transaction tx = db.beginTx()){
            for (Node mailNode : mailToIssueMap.keySet()){
                for (Node issueNode:mailToIssueMap.get(mailNode)){
                    mailNode.createRelationshipTo(issueNode, RelationshipType.withName(MAIL_TO_ISSUE));
                }
            }
            tx.success();
        }
    }

    public void getMailAndIssueMap() {
        try (Transaction tx = db.beginTx()){
            ResourceIterator<Node> nodes = db.getAllNodes().iterator();
            while (nodes.hasNext()){
                Node node = nodes.next();
                if (!node.getLabels().iterator().hasNext())
                    continue;
                if (node.hasLabel(Label.label(PfrPluginForMailList.MAIL))){
                    String content = (String)node.getProperty(PfrPluginForMailList.MAIL_BODY);
                    mailMap.put(content, node);
                }
                else if (node.hasLabel(Label.label(PfrPluginForIssueTracker.ISSUE))) {
                    String name = (String)node.getProperty(PfrPluginForIssueTracker.ISSUE_NAME);
                    nameToIssueMap.put(name, node);
                    //System.out.println(node.getProperty(PfrPluginForIssueTracker.ISSUE_NAME));
                }
            }
            tx.success();
        }
    }
}
