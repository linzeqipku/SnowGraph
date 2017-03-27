package extractors.linkers.mailtoissue;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import extractors.parsers.jira.JiraKnowledgeExtractor;
import extractors.parsers.mail.MailListKnowledgeExtractor;
import framework.KnowledgeExtractor;
import org.neo4j.graphdb.*;
import framework.annotations.RelationshipDeclaration;
/**
 * Created by laurence on 16-11-27.
 */
public class MailToIssueKnowlegeExtractor implements KnowledgeExtractor{
    @RelationshipDeclaration
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
            String patternForJira = "https?://issues\\.apache\\.org/jira/.*/([a-zA-Z]+-(\\d+))";
            String patternForBugzilla = "https?://issues\\.apache\\.org/bugzilla/.*?cgi\\?(id=(\\d+))";
            Pattern regex = Pattern.compile(patternForJira);
            Matcher idMatcher = regex.matcher(content);
            HashSet<String> issueNameSet = new HashSet<>();
            while (idMatcher.find()) {
                String issueName = idMatcher.group(1);
                issueNameSet.add(issueName);
            }
            ArrayList<Node> issueList = new ArrayList<>();
            for (String name : issueNameSet) {
                Node node = nameToIssueMap.get(name);
                if (node != null) {
                    issueList.add(node);
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
            for (Node node : db.getAllNodes()) {
                if (!node.getLabels().iterator().hasNext())
                    continue;
                if (node.hasLabel(Label.label(MailListKnowledgeExtractor.MAIL))) {
                    String content = (String) node.getProperty(MailListKnowledgeExtractor.MAIL_BODY);
                    mailMap.put(content, node);
                } else if (node.hasLabel(Label.label(JiraKnowledgeExtractor.ISSUE))) {
                    String name = (String) node.getProperty(JiraKnowledgeExtractor.ISSUE_NAME);
                    nameToIssueMap.put(name, node);
                    //System.out.println(node.getProperty(IssueTrackerKnowledgeExtractor.ISSUE_NAME));
                }
            }
            tx.success();
        }
    }
}
