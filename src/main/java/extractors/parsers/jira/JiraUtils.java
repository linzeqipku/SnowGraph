package extractors.parsers.jira;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import extractors.parsers.jira.entity.IssueCommentInfo;
import extractors.parsers.jira.entity.IssueInfo;
import extractors.parsers.jira.entity.IssueUserInfo;
import extractors.parsers.jira.entity.PatchInfo;

public class JiraUtils {

    public static void createIssueNode(IssueInfo issueInfo, Node node) {
        node.addLabel(Label.label(JiraKnowledgeExtractor.ISSUE));
        node.setProperty(JiraKnowledgeExtractor.ISSUE_ID, issueInfo.getIssueId());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_NAME, issueInfo.getIssueName());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_SUMMARY, issueInfo.getSummary());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_TYPE, issueInfo.getType());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_STATUS, issueInfo.getStatus());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_PRIORITY, issueInfo.getPriority());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_RESOLUTION, issueInfo.getResolution());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_VERSIONS, issueInfo.getVersions());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_FIX_VERSIONS, issueInfo.getFixVersions());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_COMPONENTS, issueInfo.getComponents());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_LABELS, issueInfo.getLabels());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_DESCRIPTION, issueInfo.getDescription());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_CREATOR_NAME, issueInfo.getCrearorName());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_ASSIGNEE_NAME, issueInfo.getAssigneeName());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_REPORTER_NAME, issueInfo.getReporterName());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_CREATED_DATE, issueInfo.getCreatedDate());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_UPDATED_DATE, issueInfo.getUpdatedDate());
        node.setProperty(JiraKnowledgeExtractor.ISSUE_RESOLUTION_DATE, issueInfo.getResolutionDate());
    }

    public static void createPatchNode(PatchInfo patchInfo, Node node) {
        node.addLabel(Label.label(JiraKnowledgeExtractor.PATCH));
        node.setProperty(JiraKnowledgeExtractor.PATCH_ISSUE_ID, patchInfo.getIssueId());
        node.setProperty(JiraKnowledgeExtractor.PATCH_ID, patchInfo.getPatchId());
        node.setProperty(JiraKnowledgeExtractor.PATCH_NAME, patchInfo.getPatchName());
        node.setProperty(JiraKnowledgeExtractor.PATCH_CONTENT, patchInfo.getContent());
        node.setProperty(JiraKnowledgeExtractor.PATCH_CREATOR_NAME, patchInfo.getCreatorName());
        node.setProperty(JiraKnowledgeExtractor.PATCH_CREATED_DATE, patchInfo.getCreatedDate());
    }

    public static void createIssueCommentNode(IssueCommentInfo issueCommentInfo, Node node) {
        node.addLabel(Label.label(JiraKnowledgeExtractor.ISSUECOMMENT));
        node.setProperty(JiraKnowledgeExtractor.ISSUECOMMENT_ID, issueCommentInfo.getCommentId());
        node.setProperty(JiraKnowledgeExtractor.ISSUECOMMENT_BODY, issueCommentInfo.getBody());
        node.setProperty(JiraKnowledgeExtractor.ISSUECOMMENT_CREATOR_NAME, issueCommentInfo.getCreatorName());
        node.setProperty(JiraKnowledgeExtractor.ISSUECOMMENT_UPDATER_NAME, issueCommentInfo.getUpdaterName());
        node.setProperty(JiraKnowledgeExtractor.ISSUECOMMENT_CREATED_DATE, issueCommentInfo.getCreatedDate());
        node.setProperty(JiraKnowledgeExtractor.ISSUECOMMENT_UPDATED_DATE, issueCommentInfo.getUpdatedDate());
    }

    public static void createIssueUserNode(IssueUserInfo issueUserInfo, Node node) {
        node.addLabel(Label.label(JiraKnowledgeExtractor.ISSUEUSER));
        node.setProperty(JiraKnowledgeExtractor.ISSUEUSER_NAME, issueUserInfo.getName());
        node.setProperty(JiraKnowledgeExtractor.ISSUEUSER_EMAIL_ADDRESS, issueUserInfo.getName());
        node.setProperty(JiraKnowledgeExtractor.ISSUEUSER_DISPLAY_NAME, issueUserInfo.getName());
        node.setProperty(JiraKnowledgeExtractor.ISSUEUSER_ACTIVE, issueUserInfo.getName());
    }

}
