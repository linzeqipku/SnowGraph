package crawlers.issuetracker.crawltask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import discretgraphs.issuetracker.entity.IssueInfo;

/*
 * Crawl Issue meta data, 
 * then extract issue entities, finally return extracted issue entities. 
 */
public class IssueMetadataCrawlTask extends CrawlTask<List<IssueInfo>> {
	private String projectName;
	
	public IssueMetadataCrawlTask(String strURL, String fileSavedPath,String projectName) {
		super(strURL, fileSavedPath);
		this.projectName = projectName;//the context of the task.
	}

	@Override
	protected List<IssueInfo> handle(String content) {
		List<IssueInfo> issueMetadataList = extractIssueMetaData(content);
		//append context information (projectName) for each issue to make tasks scalable
		for(IssueInfo issue: issueMetadataList){
			issue.setProjectName(projectName);
		}
		return issueMetadataList;
	}
	
	/*
	 * Extract issue basic data from given input (JSON format).
	 * One demo input as following:
	 * 
			{
				expand: "names,schema",
				startAt: 0,
				maxResults: 1,
				total: 6946,
				issues: [
					{
						expand: "operations,editmeta,changelog,transitions,renderedFields",
						id: "12929421",
						self: "https://issues.apache.org/jira/rest/api/2/issue/12929421",
						key: "LUCENE-6973"
					}
				]
			}
			
		the expected result of above demo input is [{"issueId":"12929421","issueName":"LUCENE-6973"}]
	 */
	private List<IssueInfo> extractIssueMetaData(String content){
		if(content == null){
			return Collections.emptyList();
		}
		
		List<IssueInfo> issueMetadataList = new ArrayList<>();
		
		JSONObject json = new JSONObject(content);
		JSONArray array = (JSONArray) json.get("issues");
		int issueNum = array.length();
		for(int i=0;i<issueNum;i++){
			JSONObject issue = array.getJSONObject(i);
			String issueId = issue.getString("id");
			String issueName = issue.getString("key");
			
			IssueInfo issueEntity = new IssueInfo();
			issueEntity.setIssueId(issueId);
			issueEntity.setIssueName(issueName);
			
			issueMetadataList.add(issueEntity);
		}
		return issueMetadataList;
	}
	
	public static void main(String[] args) throws Exception{
		String projectName = "lucene";
		String issueMetadataURL = "https://issues.apache.org/jira/rest/api/2/search?jql=project=LUCENE&startAt=0&maxResults=100&fields=id,key";
		String fileSavedPath = null;//null indicates no need to save crawled content.
		
		IssueMetadataCrawlTask issueMetadataTask = new IssueMetadataCrawlTask(issueMetadataURL,fileSavedPath,projectName);
		for(IssueInfo issueMetadata: issueMetadataTask.call()){
			System.out.println(issueMetadata.getProjectName() + "/" + issueMetadata.getIssueId() + "/" + issueMetadata.getIssueName());
		}
	}
}
