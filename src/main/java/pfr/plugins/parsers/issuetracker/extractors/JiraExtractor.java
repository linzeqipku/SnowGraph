package pfr.plugins.parsers.issuetracker.extractors;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import pfr.plugins.parsers.issuetracker.IssueTrackerExtractor;
import pfr.plugins.parsers.issuetracker.entity.IssueCommentInfo;
import pfr.plugins.parsers.issuetracker.entity.IssueInfo;
import pfr.plugins.parsers.issuetracker.entity.IssueLink;
import pfr.plugins.parsers.issuetracker.entity.IssueUserInfo;
import pfr.plugins.parsers.issuetracker.entity.PatchInfo;
import pfr.plugins.parsers.mail.utils.EmailAddressDecoder;

public class JiraExtractor implements IssueTrackerExtractor{
	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	private Map<String,IssueUserInfo> userMap = new HashMap<String,IssueUserInfo>();
	private Map<String,String> patchContentMap = new HashMap<String,String>();
	String issueFolderPath=null;
	
	public void setIssueFolderPath(String path){
		this.issueFolderPath=path;
	}
	
	public List<IssueInfo> extract(){
		List<IssueInfo> issues = new ArrayList<>();
		
		File issuesFolder = new File(issueFolderPath);
		for(File oneIssueFolder: issuesFolder.listFiles()){//An issue folder
			//handle with an issue
			for(File issueFileOrPatchesFolder: oneIssueFolder.listFiles()){				
				String fileName = issueFileOrPatchesFolder.getName();
				if(fileName.endsWith(".json")){//issue file
					System.out.println(fileName);
					IssueInfo issueInfo = handleWithAnIssue(issueFileOrPatchesFolder);
					issues.add(issueInfo);
				}else if(fileName.equals("patches")){//patches folder
					for(File onePatchFolder: issueFileOrPatchesFolder.listFiles()){
						String patchId = onePatchFolder.getName();
						System.out.print(patchId + "\t");
						for(File patchFile: onePatchFolder.listFiles()){
							String patchName = patchFile.getName();
							System.out.println(patchName);
							
							//handle with a patch
							handleWithAPatch(patchId,patchFile);
						}
					}
				}
			}
		}
		
		//update patch content
		for(IssueInfo issue:issues){
			for(PatchInfo patch: issue.getPatchList()){
				String patchId = patch.getPatchId();
				String patchContent = patchContentMap.get(patchId);
				patch.setContent(patchContent);
			}
		}
		
		return issues;
	}
	
	private IssueInfo handleWithAnIssue(File issueFile) {
		IssueInfo issueInfo = new IssueInfo();

		String jsonContent = null;
		try {
			jsonContent = FileUtils.readFileToString(issueFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(jsonContent == null){
			return null;
		}
		
		JSONObject root = new JSONObject(jsonContent);
		String issueId = root.getString("id");
		String issueName = root.getString("key");
		
		JSONObject fields = root.getJSONObject("fields");
		
		String type = "";
		if(!fields.isNull("issuetype")){
			type = fields.getJSONObject("issuetype").optString("name");
		}
		
		String fixVersions = getVersions(fields,"fixVersions");
		String versions = getVersions(fields,"versions");
		String resolution = "";
		if(!fields.isNull("resolution")){
			resolution = fields.getJSONObject("resolution").optString("name");
		}
	
		String priority = "";
		if(!fields.isNull("priority")){
			priority = fields.getJSONObject("priority").optString("name");;
		}
			
		
		String status = "";
		if(!fields.isNull("status")){
			status = fields.getJSONObject("status").optString("name");
		}
		
		//LUCENE-658 no description
		String description = fields.optString("description");
		String summary = fields.optString("summary");
		
		String resolutionDate = fields.optString("resolutiondate");
		String createDate = fields.optString("created");
		String updateDate = fields.optString("updated");
		
		IssueUserInfo assignee = getUser(fields,"assignee");
		IssueUserInfo creator = getUser(fields,"creator");
		IssueUserInfo reporter = getUser(fields,"reporter");
		
		//labels
		String labels = "";
		JSONArray jsonLabels = fields.optJSONArray("labels");
		if(jsonLabels != null){
			int len = jsonLabels.length();
			for(int i=0;i<len;i++){
				String label = jsonLabels.optString(i);
				labels += label;
				if(i != len - 1){
					labels += ",";
				}
			}
		}
		
		
		//components
		String components = "";
		JSONArray jsonComponents = fields.optJSONArray("components");
		if(jsonComponents != null){
			int len = jsonComponents.length();
			for(int i=0;i<len;i++){
				String component = jsonComponents.getJSONObject(i).optString("name");
				components += component;
				if(i != len-1){
					components += ",";
				}
			}
		}
		
		JSONArray jsonIssueLinks = fields.getJSONArray("issuelinks");
		int issueLinkNum = jsonIssueLinks.length();
		for(int i=0;i<issueLinkNum;i++){
			JSONObject jsonIssueLink = jsonIssueLinks.getJSONObject(i);
			String name = jsonIssueLink.getJSONObject("type").getString("name");
			String linkIssueId = "";
			if(jsonIssueLink.has("inwardIssue")){
				linkIssueId = jsonIssueLink.getJSONObject("inwardIssue").getString("id");
				issueInfo.addInwardIssueLink(new IssueLink(name,linkIssueId));
//				System.out.printf("InwardIssue: name:%s,linkIssueId:%s\n",name,linkIssueId);
			}else if(jsonIssueLink.has("outwardIssue")){
				linkIssueId = jsonIssueLink.getJSONObject("outwardIssue").getString("id");
				issueInfo.addOutwardIssueLink(new IssueLink(name,linkIssueId));
//				System.out.printf("outwardIssue: name:%s,linkIssueId:%s\n",name,linkIssueId);
			}else{
				//do nothing
			}
		}
		
		JSONArray jsonCommentArr = null;
		if(!fields.isNull("comment")){
			jsonCommentArr = fields.getJSONObject("comment").optJSONArray("comments");	
		}
		
		List<IssueCommentInfo> commentList = getComments(jsonCommentArr);
		
		JSONArray jsonHistoryArr = null;
		if(!root.isNull("changelog")){
			jsonHistoryArr = root.getJSONObject("changelog").optJSONArray("histories");	
		}
		List<PatchInfo> patchList = getPatches(jsonHistoryArr);
		for(PatchInfo patch:patchList){
			patch.setIssueId(issueId);
		}
		
		issueInfo.setIssueId(issueId);
		issueInfo.setIssueName(issueName);
		issueInfo.setType(type);
		issueInfo.setFixVersions(fixVersions);
		issueInfo.setResolution(resolution);
		issueInfo.setResolutionDate(resolutionDate);
		issueInfo.setPriority(priority);
		issueInfo.setLabels(labels);
		issueInfo.setVersions(versions);
		issueInfo.setStatus(status);
		issueInfo.setComponents(components);
		issueInfo.setDescription(description);
		issueInfo.setSummary(summary);
		
		if(assignee != null){
			issueInfo.setAssigneeName(assignee.getName());
		}
		if(creator != null){
			issueInfo.setCrearorName(creator.getName());	
		}
		if(reporter != null){
			issueInfo.setReporterName(reporter.getName());	
		}
		
		issueInfo.setCreatedDate(createDate);
		issueInfo.setUpdatedDate(updateDate);
		
		issueInfo.setCommentList(commentList);
		issueInfo.setPatchList(patchList);
		
		return issueInfo;
	}
	
	private List<IssueCommentInfo> getComments(JSONArray jsonCommentArr){
		if(jsonCommentArr == null){
			return Collections.emptyList();
		}
		
		List<IssueCommentInfo> commentList = new ArrayList<>();
		int len = jsonCommentArr.length();
		for(int i=0;i<len;i++){
			JSONObject jsonComment = jsonCommentArr.getJSONObject(i);
			String id = jsonComment.optString("id");
			String body = jsonComment.optString("body");
			
			IssueUserInfo author = getUser(jsonComment,"author");
			IssueUserInfo updateAuthor = getUser(jsonComment,"updateAuthor");
			
			String createdDate = jsonComment.optString("created");
			String updatedDate = jsonComment.optString("updated");
			
			String authorName = null, updateAuthorName = null;
			if(author != null){
				authorName = author.getName();
			}
			if(updateAuthor != null){
				updateAuthorName = updateAuthor.getName();
			}
			IssueCommentInfo comment = new IssueCommentInfo(id,body,authorName,updateAuthorName,createdDate,updatedDate);
			commentList.add(comment);
		}
		return commentList;
	}
	
	private List<PatchInfo> getPatches(JSONArray jsonHistoryArr){
		if(jsonHistoryArr == null){
			return Collections.emptyList();
		}
		
		List<PatchInfo> patches = new ArrayList<>();
		int hisNum = jsonHistoryArr.length();
		for(int i=0;i<hisNum;i++){
			JSONObject history = jsonHistoryArr.getJSONObject(i);
			JSONArray items = history.optJSONArray("items");
			if(items == null) continue;
			
			int itemNum = items.length();
			for(int j=0;j<itemNum;j++){
				JSONObject item = items.getJSONObject(j);
				String to = item.optString("to");
				String toString = item.optString("toString");
				
				//not a patch
				if(!to.matches("^\\d{1,19}$") || !toString.endsWith(".patch")){
					continue;
				}
				
				String patchId = to;
				String patchName = toString;
				
				IssueUserInfo author = getUser(history,"author");
				String createdDate = history.optString("created");
				
				String authorName = null;
				if(author != null){
					authorName = author.getName();
				}
				PatchInfo patchInfo = new PatchInfo(patchId,patchName,authorName,createdDate);
				patches.add(patchInfo);
			}
		}
		return patches;
	}
	
	private String getVersions(JSONObject jsonObj, String key){
		String versions = "";
		JSONArray jsonVersions = jsonObj.optJSONArray(key);
		if(jsonVersions == null){
			return versions;
		}
		
		int versionNum = jsonVersions.length();
		for(int i=0;i<versionNum;i++){
			JSONObject fixVersion = jsonVersions.getJSONObject(i);
			String version = fixVersion.optString("name");
			versions += version;
			
			if(i != versionNum-1){
				versions += ",";
			}
		}
		return versions;
	}
	
	@Deprecated
	private Date getDate(JSONObject jsonObj,String key){
		if(jsonObj.isNull(key)){
			return null;
		}
		
		String strDate = jsonObj.optString(key); 
		try {
			Date date = FORMATTER.parse(strDate);
			return date;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	private IssueUserInfo getUser(JSONObject jsonObj, String key){
		if(jsonObj.isNull(key)){
			return null;
		}
		
		JSONObject userJsonObj = jsonObj.getJSONObject(key);
		String name = userJsonObj.optString("name");
		String emailAddress = userJsonObj.optString("emailAddress");
		String displayName = userJsonObj.optString("displayName");
		boolean active = userJsonObj.optBoolean("active");
		
		IssueUserInfo user = new IssueUserInfo(name,EmailAddressDecoder.decode(emailAddress),displayName,active);
		userMap.put(name, user);
		return user;
	}
	
	/*
	 * Get patch file's content, then put (patchId, patchContent) to patchContentMap.
	 */
	private void handleWithAPatch(String patchId,File patchFile){
		try {
			String patchContent = FileUtils.readFileToString(patchFile);
			patchContentMap.put(patchId, patchContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Map<String, IssueUserInfo> getUserMap() {
		return userMap;
	}
}
