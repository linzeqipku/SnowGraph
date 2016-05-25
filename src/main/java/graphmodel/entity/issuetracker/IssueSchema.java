package graphmodel.entity.issuetracker;

import org.neo4j.graphdb.Node;

import discretgraphs.issuetracker.entity.IssueInfo;
import utils.NodePropertySetterUtil;
import graphmodel.ManageElements;
import graphmodel.entity.Schema;

public class IssueSchema extends Schema{
	public static final String UUID = "uuid";
	public static final String PROJECT_NAME = "projectName";
	public static final String ISSUE_ID = "issueId";
	public static final String ISSUE_NAME = "issueName";
	public static final String SUMMARY = "summary";
	public static final String TYPE = "type";
	public static final String STATUS = "status";
	public static final String PRIORITY = "priority";
	public static final String RESOLUTION = "resolution";
	public static final String VERSIONS = "versions";
	public static final String FIX_VERSIONS = "fixVersions";
	public static final String COMPONENTS = "components";
	public static final String LABELS = "labels";
	public static final String DESCRIPTION = "description";
	public static final String CREATOR_NAME = "crearorName";
	public static final String ASSIGNEE_NAME = "assigneeName";
	public static final String REPORTER_NAME = "reporterName";
	public static final String CREATED_DATE = "createdDate";
	public static final String UPDATED_DATE = "updatedDate";
	public static final String RESOLUTION_DATE = "resolutionDate";
	
	private IssueInfo issueInfo;
	
	public IssueSchema(Node node,IssueInfo issueInfo){
		this.node = node;
		node.addLabel(ManageElements.Labels.ISSUE);
		this.issueInfo = issueInfo;
		
		NodePropertySetterUtil setterUtil = NodePropertySetterUtil.getInstance();
		setterUtil.setNodeProperty(node, UUID, issueInfo.getUuid())
				  .setNodeProperty(node, PROJECT_NAME, issueInfo.getProjectName())
				  .setNodeProperty(node, ISSUE_ID, issueInfo.getIssueId())
				  .setNodeProperty(node, ISSUE_NAME, issueInfo.getIssueName())
				  .setNodeProperty(node, SUMMARY, issueInfo.getSummary())
				  .setNodeProperty(node, TYPE, issueInfo.getType())
				  .setNodeProperty(node, STATUS, issueInfo.getStatus())
				  .setNodeProperty(node, PRIORITY, issueInfo.getPriority())
				  .setNodeProperty(node, RESOLUTION, issueInfo.getResolution())
				  .setNodeProperty(node, VERSIONS, issueInfo.getVersions())
				  .setNodeProperty(node, FIX_VERSIONS, issueInfo.getFixVersions())
				  .setNodeProperty(node, COMPONENTS, issueInfo.getComponents())
				  .setNodeProperty(node, LABELS, issueInfo.getLabels())
				  .setNodeProperty(node, DESCRIPTION, issueInfo.getDescription())
				  .setNodeProperty(node, CREATOR_NAME, issueInfo.getCrearorName())
				  .setNodeProperty(node, ASSIGNEE_NAME, issueInfo.getAssigneeName())
				  .setNodeProperty(node, REPORTER_NAME, issueInfo.getReporterName())
				  .setNodeProperty(node, CREATED_DATE, issueInfo.getCreatedDate())
				  .setNodeProperty(node, UPDATED_DATE, issueInfo.getUpdatedDate())
				  .setNodeProperty(node, RESOLUTION_DATE, issueInfo.getResolutionDate());
	}

	public IssueInfo getIssueInfo() {
		return issueInfo;
	}
}
