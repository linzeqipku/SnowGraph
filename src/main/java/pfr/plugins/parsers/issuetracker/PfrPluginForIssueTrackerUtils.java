package pfr.plugins.parsers.issuetracker;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import pfr.plugins.parsers.issuetracker.entity.IssueCommentInfo;
import pfr.plugins.parsers.issuetracker.entity.IssueInfo;
import pfr.plugins.parsers.issuetracker.entity.IssueUserInfo;
import pfr.plugins.parsers.issuetracker.entity.PatchInfo;

public class PfrPluginForIssueTrackerUtils
{

	public static void createIssueNode(IssueInfo issueInfo, Node node){
		node.addLabel(Label.label(PfrPluginForIssueTracker.ISSUE));
		node.setProperty(PfrPluginForIssueTracker.ISSUE_ID, issueInfo.getIssueId());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_NAME, issueInfo.getIssueName());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_SUMMARY, issueInfo.getSummary());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_TYPE, issueInfo.getType());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_STATUS, issueInfo.getStatus());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_PRIORITY, issueInfo.getPriority());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_RESOLUTION, issueInfo.getResolution());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_VERSIONS, issueInfo.getVersions());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_FIX_VERSIONS, issueInfo.getFixVersions());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_COMPONENTS, issueInfo.getComponents());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_LABELS, issueInfo.getLabels());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_DESCRIPTION, issueInfo.getDescription());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_CREATOR_NAME, issueInfo.getCrearorName());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_ASSIGNEE_NAME, issueInfo.getAssigneeName());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_REPORTER_NAME, issueInfo.getReporterName());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_CREATED_DATE, issueInfo.getCreatedDate());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_UPDATED_DATE, issueInfo.getUpdatedDate());
		node.setProperty(PfrPluginForIssueTracker.ISSUE_RESOLUTION_DATE, issueInfo.getResolutionDate());
	}
	
	public static void createPatchNode(PatchInfo patchInfo, Node node){
		node.addLabel(Label.label(PfrPluginForIssueTracker.PATCH));
		node.setProperty(PfrPluginForIssueTracker.PATCH_ISSUE_ID, patchInfo.getIssueId());
		node.setProperty(PfrPluginForIssueTracker.PATCH_ID, patchInfo.getPatchId());
		node.setProperty(PfrPluginForIssueTracker.PATCH_NAME, patchInfo.getPatchName());
		node.setProperty(PfrPluginForIssueTracker.PATCH_CONTENT, patchInfo.getContent());
		node.setProperty(PfrPluginForIssueTracker.PATCH_CREATOR_NAME, patchInfo.getCreatorName());
		node.setProperty(PfrPluginForIssueTracker.PATCH_CREATED_DATE, patchInfo.getCreatedDate());
	}
	
	public static void createIssueCommentNode(IssueCommentInfo issueCommentInfo, Node node){
		node.addLabel(Label.label(PfrPluginForIssueTracker.ISSUECOMMENT));
		node.setProperty(PfrPluginForIssueTracker.ISSUECOMMENT_ID, issueCommentInfo.getCommentId());
		node.setProperty(PfrPluginForIssueTracker.ISSUECOMMENT_BODY, issueCommentInfo.getBody());
		node.setProperty(PfrPluginForIssueTracker.ISSUECOMMENT_CREATOR_NAME, issueCommentInfo.getCreatorName());
		node.setProperty(PfrPluginForIssueTracker.ISSUECOMMENT_UPDATER_NAME, issueCommentInfo.getUpdaterName());
		node.setProperty(PfrPluginForIssueTracker.ISSUECOMMENT_CREATED_DATE, issueCommentInfo.getCreatedDate());
		node.setProperty(PfrPluginForIssueTracker.ISSUECOMMENT_UPDATED_DATE, issueCommentInfo.getUpdatedDate());
	}
	
	public static void createIssueUserNode(IssueUserInfo issueUserInfo, Node node){
		node.addLabel(Label.label(PfrPluginForIssueTracker.ISSUEUSER));
		node.setProperty(PfrPluginForIssueTracker.ISSUEUSER_NAME, issueUserInfo.getName());
		node.setProperty(PfrPluginForIssueTracker.ISSUEUSER_EMAIL_ADDRESS, issueUserInfo.getName());
		node.setProperty(PfrPluginForIssueTracker.ISSUEUSER_DISPLAY_NAME, issueUserInfo.getName());
		node.setProperty(PfrPluginForIssueTracker.ISSUEUSER_ACTIVE, issueUserInfo.getName());
	}
	
}
