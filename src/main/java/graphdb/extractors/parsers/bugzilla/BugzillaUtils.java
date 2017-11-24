package graphdb.extractors.parsers.bugzilla;

import graphdb.extractors.parsers.bugzilla.entity.BugCommentInfo;
import graphdb.extractors.parsers.bugzilla.entity.BugInfo;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

/**
 * Created by xiaohan on 2017/4/4.
 */
class BugzillaUtils {

    public static void creatBugzillaIssueNode(BugInfo bugInfo, Node node) {
        node.addLabel(Label.label(BugzillaExtractor.BUGZILLAISSUE));
        node.setProperty(BugzillaExtractor.ISSUE_BUGID, bugInfo.getBugId());
        node.setProperty(BugzillaExtractor.ISSUE_CREATIONTS, bugInfo.getCreationTs());
        node.setProperty(BugzillaExtractor.ISSUE_SHORTDESC, bugInfo.getShortDesc());
        node.setProperty(BugzillaExtractor.ISSUE_DELTATS, bugInfo.getDeltaTs());
        node.setProperty(BugzillaExtractor.ISSUE_CLASSIFICATION, bugInfo.getClassification());
        node.setProperty(BugzillaExtractor.ISSUE_PRODUCT, bugInfo.getProduct());
        node.setProperty(BugzillaExtractor.ISSUE_COMPONENT, bugInfo.getComponent());
        node.setProperty(BugzillaExtractor.ISSUE_VERSION, bugInfo.getVersion());
        node.setProperty(BugzillaExtractor.ISSUE_REPPLATFORM, bugInfo.getRepPlatform());
        node.setProperty(BugzillaExtractor.ISSUE_OPSYS, bugInfo.getOpSys());
        node.setProperty(BugzillaExtractor.ISSUE_BUGSTATUS, bugInfo.getBugStatus());
        node.setProperty(BugzillaExtractor.ISSUE_RESOLUTION, bugInfo.getResolution());
        node.setProperty(BugzillaExtractor.ISSUE_PRIORITY, bugInfo.getPriority());
        node.setProperty(BugzillaExtractor.ISSUE_BUGSEVERITIY, bugInfo.getBugSeverity());
        node.setProperty(BugzillaExtractor.ISSUE_REPROTER, bugInfo.getReporter());
        node.setProperty(BugzillaExtractor.ISSUE_REPROTERNAME, bugInfo.getReporterName());
        node.setProperty(BugzillaExtractor.ISSUE_ASSIGNEDTO, bugInfo.getAssignedTo());
        node.setProperty(BugzillaExtractor.ISSUE_ASSIGNEENAME, bugInfo.getAssignedToName());
    }

    public static void creatIssueCommentNode(BugCommentInfo commentInfo, Node node) {
        node.addLabel(Label.label(BugzillaExtractor.ISSUECOMMENT));
        node.setProperty(BugzillaExtractor.COMMENT_ID, commentInfo.getCommentId());
        node.setProperty(BugzillaExtractor.COMMENT_WHO, commentInfo.getWho());
        node.setProperty(BugzillaExtractor.COMMENT_NAME, commentInfo.getWhoName());
        node.setProperty(BugzillaExtractor.COMMENT_BUGWHEN, commentInfo.getBugWhen());
        node.setProperty(BugzillaExtractor.COMMENT_THETEXT, commentInfo.getThetext());
    }

    public static void creatBugzillaUserNode(String id, String name, Node node) {
        node.addLabel(Label.label(BugzillaExtractor.BUGZILLAUSER));
        node.setProperty(BugzillaExtractor.USER_ID, id);
        node.setProperty(BugzillaExtractor.USER_NAME, name);
    }
}
