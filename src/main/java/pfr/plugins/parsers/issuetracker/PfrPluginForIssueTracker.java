package pfr.plugins.parsers.issuetracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import pfr.PFR;
import pfr.annotations.ConceptDeclaration;
import pfr.annotations.PropertyDeclaration;
import pfr.annotations.RelationDeclaration;
import pfr.plugins.parsers.issuetracker.entity.IssueCommentInfo;
import pfr.plugins.parsers.issuetracker.entity.IssueInfo;
import pfr.plugins.parsers.issuetracker.entity.IssueLink;
import pfr.plugins.parsers.issuetracker.entity.IssueUserInfo;
import pfr.plugins.parsers.issuetracker.entity.PatchInfo;

public class PfrPluginForIssueTracker implements PFR{
	
	@ConceptDeclaration public static final String ISSUE="Issue";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_ID = "id";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_NAME = "name";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_SUMMARY = "summary";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_TYPE = "type";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_STATUS = "status";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_PRIORITY = "priority";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_RESOLUTION = "resolution";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_VERSIONS = "versions";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_FIX_VERSIONS = "fixVersions";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_COMPONENTS = "components";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_LABELS = "labels";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_DESCRIPTION = "description";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_CREATOR_NAME = "crearorName";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_ASSIGNEE_NAME = "assigneeName";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_REPORTER_NAME = "reporterName";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_CREATED_DATE = "createdDate";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_UPDATED_DATE = "updatedDate";
	@PropertyDeclaration(parent = ISSUE)public static final String ISSUE_RESOLUTION_DATE = "resolutionDate";
	
	@ConceptDeclaration public static final String PATCH="Patch";
	@PropertyDeclaration(parent = PATCH)public static final String PATCH_ISSUE_ID = "issueId";
	@PropertyDeclaration(parent = PATCH)public static final String PATCH_ID = "id";
	@PropertyDeclaration(parent = PATCH)public static final String PATCH_NAME = "name";
	@PropertyDeclaration(parent = PATCH)public static final String PATCH_CONTENT = "content";
	@PropertyDeclaration(parent = PATCH)public static final String PATCH_CREATOR_NAME = "creatorName";
	@PropertyDeclaration(parent = PATCH)public static final String PATCH_CREATED_DATE = "createdDate";
	
	@ConceptDeclaration public static final String ISSUECOMMENT="IssueComment";
	@PropertyDeclaration(parent = ISSUECOMMENT)public static final String ISSUECOMMENT_ID = "id";
	@PropertyDeclaration(parent = ISSUECOMMENT)public static final String ISSUECOMMENT_BODY = "body";
	@PropertyDeclaration(parent = ISSUECOMMENT)public static final String ISSUECOMMENT_CREATOR_NAME = "creatorName";
	@PropertyDeclaration(parent = ISSUECOMMENT)public static final String ISSUECOMMENT_UPDATER_NAME = "updaterName";
	@PropertyDeclaration(parent = ISSUECOMMENT)public static final String ISSUECOMMENT_CREATED_DATE = "createdDate";
	@PropertyDeclaration(parent = ISSUECOMMENT)public static final String ISSUECOMMENT_UPDATED_DATE = "updatedDate";
	
	@ConceptDeclaration public static final String ISSUEUSER="IssueUser";
	@PropertyDeclaration(parent = ISSUEUSER)public static final String ISSUEUSER_NAME = "name";
	@PropertyDeclaration(parent = ISSUEUSER)public static final String ISSUEUSER_EMAIL_ADDRESS = "emailAddress";
	@PropertyDeclaration(parent = ISSUEUSER)public static final String ISSUEUSER_DISPLAY_NAME = "displayName";
	@PropertyDeclaration(parent = ISSUEUSER)public static final String ISSUEUSER_ACTIVE = "active";
	
	@RelationDeclaration public static final String HAVE_PATCH="have_patch";
	@RelationDeclaration public static final String HAVE_ISSUE_COMMENT="have_issue_comment";
	@RelationDeclaration public static final String ISSUE_DUPLICATE="issue_duplicate";
	@RelationDeclaration public static final String ISSUE_AUTHOR="issue_author";
	@PropertyDeclaration(parent = ISSUE_AUTHOR)public static final String ISSUE_AUTHOR_ROLES = "roles";
	@RelationDeclaration public static final String PATCH_AUTHOR="patch_author";
	@RelationDeclaration public static final String ISSUECOMMENT_AUTHOR="issueComment_author";
	@PropertyDeclaration(parent = ISSUE_AUTHOR)public static final String ISSUECOMMENT_AUTHOR_ROLES = "roles";
	
	private IssueTrackerExtractor extractor=null;
	
	public void setExtractor(IssueTrackerExtractor extractor){
		this.extractor=extractor;
	}

	public void run(GraphDatabaseService graphDb) {
		List<IssueInfo> issues = extractor.extract();
		Map<String,IssueUserInfo> issueUserMap = extractor.getUserMap();
		
		Map<String,Pair<IssueInfo, Node>> issueSchemaMap = new HashMap<String,Pair<IssueInfo, Node>>();
		List<Pair<PatchInfo, Node>> patchSchemaList = new ArrayList<Pair<PatchInfo, Node>>();
		List<Pair<IssueCommentInfo, Node>> issueCommentSchemaList = new ArrayList<Pair<IssueCommentInfo, Node>>();
		Map<String,Pair<IssueUserInfo, Node>> issueUserSchemaMap = new HashMap<String,Pair<IssueUserInfo, Node>>();
		
		try(Transaction tx = graphDb.beginTx()){
			/***************************create nodes***************************/
			//create issue nodes, patch nodes and issue comment nodes
			for(IssueInfo issueInfo: issues){
				//create issue nodes
				Node node = graphDb.createNode();
				PfrPluginForIssueTrackerUtils.createIssueNode(issueInfo, node);
				Pair<IssueInfo, Node> issueSchema = new ImmutablePair<IssueInfo, Node>(issueInfo,node);
				issueSchemaMap.put(issueSchema.getLeft().getIssueId(), issueSchema);
				
				//create patch nodes
				List<PatchInfo> patches = issueInfo.getPatchList();
				for(PatchInfo patchInfo: patches){
					Node patchNode = graphDb.createNode();
					PfrPluginForIssueTrackerUtils.createPatchNode(patchInfo, patchNode);
					Pair<PatchInfo, Node> patchSchema = new ImmutablePair<PatchInfo, Node>(patchInfo,patchNode);
					patchSchemaList.add(patchSchema);
					//create relationship from issue to one of its patch
					node.createRelationshipTo(patchNode, RelationshipType.withName(HAVE_PATCH));
				}
				
				//create issue comment nodes
				List<IssueCommentInfo> issueComments = issueInfo.getCommentList();
				for(IssueCommentInfo issueCommentInfo: issueComments){
					Node issueCommentNode = graphDb.createNode();
					PfrPluginForIssueTrackerUtils.createIssueCommentNode(issueCommentInfo, issueCommentNode);
					Pair<IssueCommentInfo, Node> issueCommentSchema = new ImmutablePair<IssueCommentInfo, Node>(issueCommentInfo, issueCommentNode);
					issueCommentSchemaList.add(issueCommentSchema);
					node.createRelationshipTo(issueCommentNode, RelationshipType.withName(HAVE_ISSUE_COMMENT));
				}
			}
			
			/***************************create relationships between Issues***************************/
			for(Pair<IssueInfo, Node> issueSchema: issueSchemaMap.values()){
				//建立入边
				for(IssueLink issueLink: issueSchema.getLeft().getInwardIssueLinks()){
					//建立Issue之间的Duplicate关系
					if(issueLink.getIssueLinkName().equalsIgnoreCase("Duplicate")){
						Pair<IssueInfo, Node> otherIssueSchema = issueSchemaMap.get(issueLink.getLinkIssueId());
						if(otherIssueSchema != null){
							otherIssueSchema.getRight().createRelationshipTo(issueSchema.getRight(), RelationshipType.withName(ISSUE_DUPLICATE));
							System.out.printf("Duplicate relationship: %s --DUPLICATE-->%s",
											  otherIssueSchema.getLeft().getIssueName(),
											  issueSchema.getLeft().getIssueName());
						}
					}
				}
			}
			
			//create issue user nodes
			for(String name: issueUserMap.keySet()){
				IssueUserInfo issueUserInfo = issueUserMap.get(name);
				Node issueUserNode = graphDb.createNode();
				PfrPluginForIssueTrackerUtils.createIssueUserNode(issueUserInfo, issueUserNode);
				Pair<IssueUserInfo, Node> issueUserSchema = new ImmutablePair<IssueUserInfo, Node>(issueUserInfo,issueUserNode);
				issueUserSchemaMap.put(name, issueUserSchema);
			}
			
			/***************************create relationships with Issue Users***************************/
			//create relationships from user to issue by creator, assignee and reporter
			for(Pair<IssueInfo, Node> issueSchema: issueSchemaMap.values()){
				Node issueNode = issueSchema.getRight();
				
				IssueInfo issueInfo = issueSchema.getLeft();
				String creatorName = issueInfo.getCrearorName();
				String assigneeName = issueInfo.getAssigneeName();
				String reporterName = issueInfo.getReporterName();
				
				HashMap<String,String> nameToRolesMap = new HashMap<String,String>();
				if(creatorName != null){
					nameToRolesMap.put(creatorName, "creator");
				}
				
				if(assigneeName != null){
					if(nameToRolesMap.containsKey(assigneeName)){
						nameToRolesMap.put(assigneeName, nameToRolesMap.get(assigneeName) + ",assignee");
					}else{
						nameToRolesMap.put(assigneeName, "assignee");
					}
				}
				
				if(reporterName != null){
					if(nameToRolesMap.containsKey(reporterName)){
						nameToRolesMap.put(reporterName, nameToRolesMap.get(reporterName) + ",reporter");
					}else{
						nameToRolesMap.put(reporterName, "reporter");
					}
				}
				
				for(String username: nameToRolesMap.keySet()){
					if(issueUserSchemaMap.containsKey(username)){
						String roles = nameToRolesMap.get(username);
						Node userNode = issueUserSchemaMap.get(username).getRight();
						Relationship relationship = userNode.createRelationshipTo(issueNode, RelationshipType.withName(ISSUE_AUTHOR));
						relationship.setProperty(ISSUE_AUTHOR_ROLES, roles);
					}
				}
			}
			
			//create relationships from user to patch by creator
			for(Pair<PatchInfo, Node> patchSchema: patchSchemaList){
				Node patchNode = patchSchema.getRight();
				PatchInfo patchInfo = patchSchema.getLeft();
				String creatorName = patchInfo.getCreatorName();
				
				if(creatorName != null && issueUserSchemaMap.containsKey(creatorName)){
					Node creatorNode = issueUserSchemaMap.get(creatorName).getRight();
					creatorNode.createRelationshipTo(patchNode, RelationshipType.withName(PATCH_AUTHOR));
				}
			}
			
			//create relationships from user to issue comment by creator and updater
			for(Pair<IssueCommentInfo, Node> issueCommentSchema: issueCommentSchemaList){
				Node issueCommentNode = issueCommentSchema.getRight();
				IssueCommentInfo issueCommentInfo = issueCommentSchema.getLeft();
				String creatorName = issueCommentInfo.getCreatorName();
				String updaterName = issueCommentInfo.getUpdaterName();
				
				HashMap<String,String> nameToRolesMap = new HashMap<String,String>();
				if(creatorName != null){
					nameToRolesMap.put(creatorName, "creator");
				}
				
				if(updaterName != null){
					if(nameToRolesMap.containsKey(updaterName)){
						nameToRolesMap.put(updaterName, nameToRolesMap.get(updaterName) + "," + "updater");
					}else{
						nameToRolesMap.put(updaterName, "updater");
					}
				}
				
				for(String username: nameToRolesMap.keySet()){
					String roles = nameToRolesMap.get(username);
					if(issueUserSchemaMap.containsKey(username)){
						Node userNode = issueUserSchemaMap.get(username).getRight();
						Relationship relationship = userNode.createRelationshipTo(issueCommentNode, RelationshipType.withName(ISSUECOMMENT_AUTHOR));
						relationship.setProperty(ISSUECOMMENT_AUTHOR_ROLES, roles);
					}
				}
			}
			
			tx.success();
		}
	}

}
