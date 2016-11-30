package extractors.parsers.issuetracker;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import extractors.parsers.issuetracker.entity.IssueCommentInfo;
import extractors.parsers.issuetracker.entity.IssueInfo;
import extractors.parsers.issuetracker.entity.IssueUserInfo;
import extractors.parsers.issuetracker.entity.PatchInfo;

public class IssueTrackerUtils {

    public static void createIssueNode(IssueInfo issueInfo, Node node) {
        node.addLabel(Label.label(IssueTrackerKnowledgeExtractor.ISSUE));
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_ID, issueInfo.getIssueId());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_NAME, issueInfo.getIssueName());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_SUMMARY, issueInfo.getSummary());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_TYPE, issueInfo.getType());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_STATUS, issueInfo.getStatus());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_PRIORITY, issueInfo.getPriority());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_RESOLUTION, issueInfo.getResolution());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_VERSIONS, issueInfo.getVersions());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_FIX_VERSIONS, issueInfo.getFixVersions());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_COMPONENTS, issueInfo.getComponents());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_LABELS, issueInfo.getLabels());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_DESCRIPTION, issueInfo.getDescription());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_CREATOR_NAME, issueInfo.getCrearorName());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_ASSIGNEE_NAME, issueInfo.getAssigneeName());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_REPORTER_NAME, issueInfo.getReporterName());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_CREATED_DATE, issueInfo.getCreatedDate());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_UPDATED_DATE, issueInfo.getUpdatedDate());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUE_RESOLUTION_DATE, issueInfo.getResolutionDate());
    }

    public static void createPatchNode(PatchInfo patchInfo, Node node) {
        node.addLabel(Label.label(IssueTrackerKnowledgeExtractor.PATCH));
        node.setProperty(IssueTrackerKnowledgeExtractor.PATCH_ISSUE_ID, patchInfo.getIssueId());
        node.setProperty(IssueTrackerKnowledgeExtractor.PATCH_ID, patchInfo.getPatchId());
        node.setProperty(IssueTrackerKnowledgeExtractor.PATCH_NAME, patchInfo.getPatchName());
        node.setProperty(IssueTrackerKnowledgeExtractor.PATCH_CONTENT, patchInfo.getContent());
        node.setProperty(IssueTrackerKnowledgeExtractor.PATCH_CREATOR_NAME, patchInfo.getCreatorName());
        node.setProperty(IssueTrackerKnowledgeExtractor.PATCH_CREATED_DATE, patchInfo.getCreatedDate());
    }

    public static void createIssueCommentNode(IssueCommentInfo issueCommentInfo, Node node) {
        node.addLabel(Label.label(IssueTrackerKnowledgeExtractor.ISSUECOMMENT));
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUECOMMENT_ID, issueCommentInfo.getCommentId());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUECOMMENT_BODY, issueCommentInfo.getBody());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUECOMMENT_CREATOR_NAME, issueCommentInfo.getCreatorName());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUECOMMENT_UPDATER_NAME, issueCommentInfo.getUpdaterName());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUECOMMENT_CREATED_DATE, issueCommentInfo.getCreatedDate());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUECOMMENT_UPDATED_DATE, issueCommentInfo.getUpdatedDate());
    }

    public static void createIssueUserNode(IssueUserInfo issueUserInfo, Node node) {
        node.addLabel(Label.label(IssueTrackerKnowledgeExtractor.ISSUEUSER));
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUEUSER_NAME, issueUserInfo.getName());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUEUSER_EMAIL_ADDRESS, issueUserInfo.getName());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUEUSER_DISPLAY_NAME, issueUserInfo.getName());
        node.setProperty(IssueTrackerKnowledgeExtractor.ISSUEUSER_ACTIVE, issueUserInfo.getName());
    }

}
