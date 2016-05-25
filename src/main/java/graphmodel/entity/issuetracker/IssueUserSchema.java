package graphmodel.entity.issuetracker;

import graphmodel.ManageElements;
import graphmodel.entity.Schema;

import org.neo4j.graphdb.Node;

import discretgraphs.issuetracker.entity.IssueUserInfo;
import utils.NodePropertySetterUtil;

public class IssueUserSchema extends Schema{
	public static final String UUID = "uuid";
	public static final String NAME = "name";
	public static final String EMAIL_ADDRESS = "emailAddress";
	public static final String DISPLAY_NAME = "displayName";
	public static final String ACTIVE = "active";
	
	private IssueUserInfo issueUserInfo;
	
	public IssueUserSchema(Node node,IssueUserInfo issueUserInfo){
		this.node = node;
		node.addLabel(ManageElements.Labels.ISSUE_USER);
		this.issueUserInfo = issueUserInfo;
		
		//set node properties based on an issue user's info
		NodePropertySetterUtil nodePropertySetterUtil = NodePropertySetterUtil.getInstance();
		nodePropertySetterUtil
				.setNodeProperty(node, UUID, issueUserInfo.getUuid())
				.setNodeProperty(node, NAME, issueUserInfo.getName())
				.setNodeProperty(node, EMAIL_ADDRESS,issueUserInfo.getEmailAddress())
				.setNodeProperty(node, DISPLAY_NAME,issueUserInfo.getDisplayName())
				.setNodeProperty(node, ACTIVE, issueUserInfo.isActive());
	}

	public IssueUserSchema(Node node){
		this.node = node;
		
		String name = node.getProperty(NAME).toString();
		String mail = node.getProperty(EMAIL_ADDRESS).toString();
		String displayName = node.getProperty(DISPLAY_NAME).toString();
		String strIsActive = node.getProperty(ACTIVE).toString();
		boolean isActive = Boolean.parseBoolean(strIsActive);
		
		this.issueUserInfo = new IssueUserInfo(name,mail,displayName,isActive);
	}
	
	public IssueUserInfo getIssueUserInfo() {
		return issueUserInfo;
	}
}
