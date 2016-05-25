package graphmodel.entity.issuetracker;

import org.neo4j.graphdb.Node;

import discretgraphs.issuetracker.entity.PatchInfo;
import utils.NodePropertySetterUtil;
import graphmodel.ManageElements;
import graphmodel.entity.Schema;

public class PatchSchema extends Schema {
	public static final String UUID = "uuid";
	public static final String PROJECT_NAME = "projectName";
	public static final String ISSUE_ID = "issueId";
	public static final String PATCH_ID = "patchId";
	public static final String PATCH_NAME = "patchName";
	public static final String CONTENT = "content";
	public static final String CREATOR_NAME = "creatorName";
	public static final String CREATED_DATE = "createdDate";
	
	private PatchInfo patchInfo;
	
	public PatchSchema(Node node, PatchInfo patchInfo){
		this.node = node;
		node.addLabel(ManageElements.Labels.PATCH);
		this.patchInfo = patchInfo;
		
		NodePropertySetterUtil setterUtil = NodePropertySetterUtil.getInstance();
		setterUtil.setNodeProperty(node, UUID, patchInfo.getUuid())
				  .setNodeProperty(node, PROJECT_NAME, patchInfo.getProjectName())
				  .setNodeProperty(node, ISSUE_ID, patchInfo.getIssueId())
				  .setNodeProperty(node, PATCH_ID, patchInfo.getPatchId())
				  .setNodeProperty(node, PATCH_NAME, patchInfo.getPatchName())
				  .setNodeProperty(node, CONTENT, patchInfo.getContent())
				  .setNodeProperty(node, CREATOR_NAME, patchInfo.getCreatorName())
				  .setNodeProperty(node, CREATED_DATE, patchInfo.getCreatedDate());
	}

	public PatchInfo getPatchInfo() {
		return patchInfo;
	}
}
