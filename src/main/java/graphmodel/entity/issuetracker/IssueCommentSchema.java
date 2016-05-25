package graphmodel.entity.issuetracker;

import org.neo4j.graphdb.Node;

import discretgraphs.issuetracker.entity.IssueCommentInfo;
import utils.NodePropertySetterUtil;
import graphmodel.ManageElements;
import graphmodel.entity.Schema;

public class IssueCommentSchema extends Schema{
	public static final String UUID = "uuid";
	public static final String COMMENT_ID = "commentId";
	public static final String BODY = "body";
	public static final String CREATOR_NAME = "creatorName";
	public static final String UPDATER_NAME = "updaterName";
	public static final String CREATED_DATE = "createdDate";
	public static final String UPDATED_DATE = "updatedDate";
	
	private IssueCommentInfo issueCommentInfo;
	
	public IssueCommentSchema(Node node,IssueCommentInfo issueCommentInfo){
		this.node = node;
		node.addLabel(ManageElements.Labels.ISSUE_COMMENT);
		this.issueCommentInfo = issueCommentInfo;
		
		NodePropertySetterUtil nodePropertySetterUtil = NodePropertySetterUtil.getInstance();
		nodePropertySetterUtil.setNodeProperty(node, UUID, issueCommentInfo.getUuid())
							  .setNodeProperty(node, COMMENT_ID, issueCommentInfo.getCommentId())
							  .setNodeProperty(node, BODY, issueCommentInfo.getBody())
							  .setNodeProperty(node, CREATOR_NAME, issueCommentInfo.getCreatorName())
							  .setNodeProperty(node, UPDATER_NAME, issueCommentInfo.getUpdaterName())
							  .setNodeProperty(node, CREATED_DATE, issueCommentInfo.getCreatedDate())
							  .setNodeProperty(node, UPDATED_DATE, issueCommentInfo.getUpdatedDate());
	}

	public IssueCommentInfo getIssueCommentInfo() {
		return issueCommentInfo;
	}
}
