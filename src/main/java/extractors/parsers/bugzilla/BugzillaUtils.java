package extractors.parsers.bugzilla;

import extractors.parsers.bugzilla.entity.BugCommentInfo;
import extractors.parsers.bugzilla.entity.BugInfo;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

/**
 * Created by xiaohan on 2017/4/4.
 */
public class BugzillaUtils {

    public static void creatBugzillaIssueNode(BugInfo bugInfo, Node node) {
        node.addLabel(Label.label(BugzillaKnowledgeExtractor.BUGZILLAISSUE));
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_BUGID, bugInfo.getBugId());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_CREATIONTS, bugInfo.getCreationTs());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_SHORTDESC, bugInfo.getShortDesc());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_DELTATS, bugInfo.getDeltaTs());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_CLASSIFICATION, bugInfo.getClassification());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_PRODUCT, bugInfo.getProduct());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_COMPONENT, bugInfo.getComponent());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_VERSION, bugInfo.getVersion());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_REPPLATFORM, bugInfo.getRepPlatform());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_OPSYS, bugInfo.getOpSys());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_BUGSTATUS, bugInfo.getBugStatus());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_RESOLUTION, bugInfo.getResolution());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_PRIORITY, bugInfo.getPriority());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_BUGSEVERITIY, bugInfo.getBugSeverity());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_REPROTER, bugInfo.getReporter());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_REPROTERNAME, bugInfo.getReporterName());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_ASSIGNEDTO, bugInfo.getAssignedTo());
        node.setProperty(BugzillaKnowledgeExtractor.ISSUE_ASSIGNEENAME, bugInfo.getAssignedToName());
    }

    public static void creatIssueCommentNode(BugCommentInfo commentInfo, Node node) {
        node.addLabel(Label.label(BugzillaKnowledgeExtractor.ISSUECOMMENT));
        node.setProperty(BugzillaKnowledgeExtractor.COMMENT_ID, commentInfo.getCommentId());
        node.setProperty(BugzillaKnowledgeExtractor.COMMENT_WHO, commentInfo.getWho());
        node.setProperty(BugzillaKnowledgeExtractor.COMMENT_NAME, commentInfo.getWhoName());
        node.setProperty(BugzillaKnowledgeExtractor.COMMENT_BUGWHEN, commentInfo.getBugWhen());
        node.setProperty(BugzillaKnowledgeExtractor.COMMENT_THETEXT, commentInfo.getThetext());
    }

    public static void creatBugzillaUserNode(String id, String name, Node node) {
        node.addLabel(Label.label(BugzillaKnowledgeExtractor.BUGZILLAUSER));
        node.setProperty(BugzillaKnowledgeExtractor.USER_ID, id);
        node.setProperty(BugzillaKnowledgeExtractor.USER_NAME, name);
    }
}
