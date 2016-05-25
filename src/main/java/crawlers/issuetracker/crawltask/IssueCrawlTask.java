package crawlers.issuetracker.crawltask;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import discretgraphs.issuetracker.entity.PatchInfo;
import utils.Config;

/*
 * Crawl an issue, then save its content to target file,
 * then extract patch entities,
 * finally return extracted patch entities   
 */
public class IssueCrawlTask extends CrawlTask<List<PatchInfo>> {

	/*context information of the issue*/
	private String projectName;
	
	public IssueCrawlTask(String strURL, String fileSavedPath,String projectName) {
		super(strURL, fileSavedPath);
		this.projectName = projectName;
	}

	@Override
	protected List<PatchInfo> handle(String issueContentAsJson) {
		List<PatchInfo> patches = extractPatchesInfo(issueContentAsJson);
		//append context information (project name) for each patch 
		for(PatchInfo patch: patches){
			patch.setProjectName(projectName);
		}
		return patches;
	}
	
	/*
	 * Extract patch entities from the given issue content in JSON format.
	 * 
	 * @param issueContentAsJson: issue content in JSON format
	 * @return the list of patch entities 
	 */
	private List<PatchInfo> extractPatchesInfo(String issueContentAsJson){
		List<PatchInfo> patches = new ArrayList<PatchInfo>();
		
		try{
			JSONObject root = new JSONObject(issueContentAsJson);
			
			JSONObject changelogObj = root.getJSONObject("changelog");
			String issueId = root.getString("id");
			
			JSONArray historiesArr = changelogObj.getJSONArray("histories");
			
			//parse each history to find patch created info iteratively.
			int hisNum = historiesArr.length();
			for(int i=0;i<hisNum;i++){
				JSONObject hisObj = historiesArr.getJSONObject(i);
				JSONArray hisItems = hisObj.getJSONArray("items");
				int hisItemNum = hisItems.length();
				
				//For a patch created info, "to" stands for patchId and "toString" stands for patchName.  
				for(int j=0;j<hisItemNum;j++){
					JSONObject hisItem = hisItems.getJSONObject(j);
				
					//get patch id if it is a patch created info
					if(hisItem.isNull("to")){//special check for Nullable field before getting its value.
						continue;
					}
					
					String patchId = hisItem.getString("to");
					//Not a POSITIVE Long indicates that it is not a patch created info
					if(!patchId.matches("^\\d{1,19}$")){
						continue;
					}
					
					//get patch name if it is a patch created info
					if(hisItem.isNull("toString")){//special check for Nullable field before getting its value.
						continue;
					}
					String patchName = hisItem.getString("toString");
					//Not a string which ends with ".patch" represents that it is not a patch created info 
					if(!patchName.endsWith(".patch")){
						continue;
					}
					
					PatchInfo patch = new PatchInfo();
					patch.setPatchId(patchId);
					patch.setPatchName(patchName);
					patch.setIssueId(issueId);
					
					patches.add(patch);
				}
			}
		}catch(Exception e){//swallow any exception when extracting patch info.
			e.printStackTrace();
		}
		
		return patches;
	}
	
	public static void main(String[] args) throws Exception{
		String projectName = "lucene";
		String issueId = "12945265";
		String issueName = "LUCENE-7053";//(Very interesting patch)
		String issueSavedPath = Config.getIssueFileSavedPath(projectName, issueId, issueName);
		
		String issueURL = "https://issues.apache.org/jira/rest/api/2/issue/" + issueId + "?expand=changelog";
		
		IssueCrawlTask issueTask = new IssueCrawlTask(issueURL,issueSavedPath,projectName);
		for(PatchInfo patch: issueTask.call()){
			System.out.println(patch.getProjectName() + "/" + patch.getIssueId() + "/" +patch.getPatchId() + "/" +patch.getPatchName());
		}
	}
}
