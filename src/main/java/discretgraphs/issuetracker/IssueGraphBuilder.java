package discretgraphs.issuetracker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import discretgraphs.GraphBuilder;
import discretgraphs.issuetracker.entity.IssueCommentInfo;
import discretgraphs.issuetracker.entity.IssueInfo;
import discretgraphs.issuetracker.entity.IssueLink;
import discretgraphs.issuetracker.entity.IssueUserInfo;
import discretgraphs.issuetracker.entity.PatchInfo;
import graphmodel.ManageElements;
import graphmodel.entity.issuetracker.IssueCommentSchema;
import graphmodel.entity.issuetracker.IssueSchema;
import graphmodel.entity.issuetracker.IssueUserSchema;
import graphmodel.entity.issuetracker.PatchSchema;

public class IssueGraphBuilder extends GraphBuilder{
	private String projectName;
	private String issueFolderPath = null;
	
	public IssueGraphBuilder(String dbPath,String issueFolderPath,String projectName) {
		super(dbPath);
		this.projectName = projectName;
		this.issueFolderPath = issueFolderPath;
		name = "IssueGraphBuilder";
	}

	public static void main(String[] args) {
		String projectName = "lucene";
		String dbPath = "data/"+projectName+"/graphdb/issue";
		String issueFolderPath = "data/lucene/source_data/issue";
		
		long beginTime = System.currentTimeMillis();
		
		IssueGraphBuilder issueGraphBuilder = new IssueGraphBuilder(dbPath,issueFolderPath,projectName);
		issueGraphBuilder.run();
		
		long usageTime = System.currentTimeMillis() - beginTime;
	}
	
	@Override
	public void run() {
		IssueExtractor extractor = new IssueExtractor();
		List<IssueInfo> issues = extractor.extract(issueFolderPath, projectName);
		Map<String,IssueUserInfo> issueUserMap = extractor.getUserMap();
		
		//delete existed database before creating a new one
		File dbFile = new File(dbPath);
		if(dbFile.exists()){
			dbFile.delete();
		}
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbFile).newGraphDatabase();
		
		Map<String,IssueSchema> issueSchemaMap = new HashMap<>();
		List<PatchSchema> patchSchemaList = new ArrayList<>();
		List<IssueCommentSchema> issueCommentSchemaList = new ArrayList<>();
		Map<String,IssueUserSchema> issueUserSchemaMap = new HashMap<>();
		
		try(Transaction tx = graphDb.beginTx()){
			/***************************create nodes***************************/
			//create issue nodes, patch nodes and issue comment nodes
			for(IssueInfo issueInfo: issues){
				//create issue nodes
				Node issueNode = graphDb.createNode();
				System.out.println("issueNode:" + issueNode);
				IssueSchema issueSchema = new IssueSchema(issueNode,issueInfo);
				issueSchemaMap.put(issueSchema.getIssueInfo().getIssueId(), issueSchema);
				
				//create patch nodes
				List<PatchInfo> patches = issueInfo.getPatchList();
				for(PatchInfo patchInfo: patches){
					Node patchNode = graphDb.createNode();
//					System.out.println(patchInfo);
					System.out.println("patchNode:" + patchNode);
					PatchSchema patchSchema = new PatchSchema(patchNode,patchInfo);
					patchSchemaList.add(patchSchema);
					
					//create relationship from issue to one of its patch
					issueNode.createRelationshipTo(patchNode, ManageElements.RelTypes.HAVE_PATCH);
				}
				
				//create issue comment nodes
				List<IssueCommentInfo> issueComments = issueInfo.getCommentList();
				for(IssueCommentInfo issueCommentInfo: issueComments){
					Node issueCommentNode = graphDb.createNode();
					IssueCommentSchema issueCommentSchema = new IssueCommentSchema(issueCommentNode, issueCommentInfo);
					issueCommentSchemaList.add(issueCommentSchema);
					
//					System.out.println(issueCommentInfo);
					System.out.println("issueCommentNode:"+issueCommentNode);
					
					//create relationship from issue to one of its comment
					issueNode.createRelationshipTo(issueCommentNode, ManageElements.RelTypes.HAVE_ISSUE_COMMENT);
				}
			}
			
			/***************************create relationships between Issues***************************/
			for(IssueSchema issueSchema: issueSchemaMap.values()){
				//建立入边
				for(IssueLink issueLink: issueSchema.getIssueInfo().getInwardIssueLinks()){
					//建立Issue之间的Duplicate关系
					if(issueLink.getIssueLinkName().equalsIgnoreCase("Duplicate")){
						IssueSchema otherIssueSchema = issueSchemaMap.get(issueLink.getLinkIssueId());
						if(otherIssueSchema != null){
							otherIssueSchema.getNode().createRelationshipTo(issueSchema.getNode(), ManageElements.RelTypes.DUPLICATE);
							System.out.printf("Duplicate relationship: %s --DUPLICATE-->%s",
											  otherIssueSchema.getIssueInfo().getIssueName(),
											  issueSchema.getIssueInfo().getIssueName());
						}
					}
				}
				
				//建立出边
				for(IssueLink issueLink: issueSchema.getIssueInfo().getOutwardIssueLinks()){
					//TODO: add outwardIssueLinks
				}
			}
			
			//create issue user nodes
			for(String name: issueUserMap.keySet()){
				IssueUserInfo issueUserInfo = issueUserMap.get(name);
				Node issueUserNode = graphDb.createNode();
				IssueUserSchema issueUserSchema = new IssueUserSchema(issueUserNode,issueUserInfo);
				
				System.out.println(issueUserInfo);
				System.out.println("issueUserNode:" + issueUserNode);
				issueUserSchemaMap.put(name, issueUserSchema);
			}
			
			/***************************create relationships with Issue Users***************************/
			//create relationships from user to issue by creator, assignee and reporter
			for(IssueSchema issueSchema: issueSchemaMap.values()){
				Node issueNode = issueSchema.getNode();
				
				IssueInfo issueInfo = issueSchema.getIssueInfo();
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
						Node userNode = issueUserSchemaMap.get(username).getNode();
						Relationship relationship = userNode.createRelationshipTo(issueNode, ManageElements.RelTypes.AUTHOR);
						relationship.setProperty("roles", roles);
					}
				}
			}
			
			//create relationships from user to patch by creator
			for(PatchSchema patchSchema: patchSchemaList){
				Node patchNode = patchSchema.getNode();
				PatchInfo patchInfo = patchSchema.getPatchInfo();
				String creatorName = patchInfo.getCreatorName();
				
				if(creatorName != null && issueUserSchemaMap.containsKey(creatorName)){
					Node creatorNode = issueUserSchemaMap.get(creatorName).getNode();
					Relationship relationship = creatorNode.createRelationshipTo(patchNode, ManageElements.RelTypes.AUTHOR);
					relationship.setProperty("roles", "creator");
				}
			}
			
			//create relationships from user to issue comment by creator and updater
			for(IssueCommentSchema issueCommentSchema: issueCommentSchemaList){
				Node issueCommentNode = issueCommentSchema.getNode();
				IssueCommentInfo issueCommentInfo = issueCommentSchema.getIssueCommentInfo();
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
						Node userNode = issueUserSchemaMap.get(username).getNode();
						Relationship relationship = userNode.createRelationshipTo(issueCommentNode, ManageElements.RelTypes.AUTHOR);
						relationship.setProperty("roles", roles);
					}
				}
			}
			
			tx.success();
		}
		
		graphDb.shutdown();
	}

}
