package crawlers.issuetracker.crawler;

import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;

import crawlers.issuetracker.crawltask.IssueCrawlTask;
import crawlers.issuetracker.crawltask.IssueMetadataCrawlTask;
import crawlers.issuetracker.crawltask.PatchCrawlTask;
import crawlers.issuetracker.exception.CrawlFailedException;
import crawlers.issuetracker.threadpool.ThreadPool;
import crawlers.issuetracker.util.CrawlerUtil;
import discretgraphs.issuetracker.entity.IssueInfo;
import discretgraphs.issuetracker.entity.PatchInfo;
import utils.Config;

public class IssueCrawler {
	
	private static final int DEFAULT_MAX_RESULTS = 100;//JIRA's maximum items per request
	
	private static final String TOTAL_ISSUE_NUM_URL_TEMPLATE = 
			"https://issues.apache.org/jira/rest/api/2/search?jql=project=%s&maxResults=0";
	private static final String ISSUE_METADATA_URL_TEMPLATE = 
			"https://issues.apache.org/jira/rest/api/2/search?jql=project=%s&startAt=%d&maxResults=%d&fields=id,key";//(projectName,startAt,maxResults)
	private static final String ISSUE_URL_TEMPLATE = 
			"https://issues.apache.org/jira/rest/api/2/issue/%s?expand=changelog";//(issue id or issue name)
	private static final String PATCH_URL_TEMPLATE = "https://issues.apache.org/jira/secure/attachment/%s/%s";//(patch id, patch name)

	
	/*
	 * Crawl the issues for the given project.
	 *
	 * @param projectName: name of the given project
	 * 
	 * @throws exceptions.CrawlFailedException;
	 * 		If fails to crawl the number of given project's issue.
	 */
	public static void crawl(String projectName) throws CrawlFailedException{
		if(projectName == null){
			throw new NullPointerException("Project name is null.");
		}
		
		long beginTimeInMs = System.currentTimeMillis();
		
		long totalIssueNum = 0;
		totalIssueNum = getTotalIssueNum(projectName);

		//get the maximum number of issues can be returned (maxResults) 
		int maxResults = validateMaxResults(projectName,DEFAULT_MAX_RESULTS);
		
		//crawl issue data
		List<IssueMetadataCrawlTask> issueMetadataTasks = new ArrayList<IssueMetadataCrawlTask>();
		
		int stepNum = maxResults;
		for(int startAt=0;startAt < totalIssueNum; startAt += stepNum){
			String issueMetaDataURL = getIssueMetadataURL(projectName, startAt,maxResults);
			System.out.println(issueMetaDataURL);
			IssueMetadataCrawlTask issueMetadataTask = new IssueMetadataCrawlTask(issueMetaDataURL,null, projectName);
			issueMetadataTasks.add(issueMetadataTask);
		}
		//execute crawling issue metadata tasks concurrently
		List<List<IssueInfo>> issuesList = ThreadPool.execute(issueMetadataTasks);
		
		//crawl issues
		List<IssueCrawlTask> issueTasks = new ArrayList<IssueCrawlTask>();
		for(List<IssueInfo> issues: issuesList){
			for(IssueInfo issue: issues){
				String issueId = issue.getIssueId();
				String issueName = issue.getIssueName();
				String projectNameOfIssue = issue.getProjectName();
				String issueURL = getIssueURL(issueId);
				String issueFileSavedPath = Config.getIssueFileSavedPath(projectNameOfIssue, issueId, issueName);
				
				IssueCrawlTask issueTask = new IssueCrawlTask(issueURL,issueFileSavedPath,projectNameOfIssue);
				issueTasks.add(issueTask);
			}
		}
		
		List<List<PatchInfo>> patchesList = ThreadPool.execute(issueTasks);
		
		//crawl patches
		List<PatchCrawlTask> patchTasks = new ArrayList<PatchCrawlTask>();
		for(List<PatchInfo> patches: patchesList){
			for(PatchInfo patch: patches){
				String projectNameOfPatch = patch.getProjectName();
				String issueIdOfPatch = patch.getIssueId();
				String patchId = patch.getPatchId();
				String patchName = patch.getPatchName();
				
				String patchURL = getPatchURL(patchId, patchName);
				String patchFileSavedPath = Config.getPatchFileSavedPath(projectNameOfPatch, issueIdOfPatch, patchId, patchName);
				
				PatchCrawlTask patchTask = new PatchCrawlTask(patchURL, patchFileSavedPath);
				patchTasks.add(patchTask);
			}
		}
		//no output at all.
		ThreadPool.execute(patchTasks);

		long usageTimeInMs = System.currentTimeMillis() - beginTimeInMs;
		long mins = usageTimeInMs/1000/60;
		long seconds = (usageTimeInMs%(1000*60))/1000;
	}
	
	/*
	 * Get the number of issues of the given project.
	 * 
	 * @param projectName: the given project name
	 *
	 * @throws exceptions.CrawlFailedException;
	 * 		If fails to crawl the number of given project's issue.
	 *
	 * @return: the number of issues of the given project
	 */
	private static long getTotalIssueNum(String projectName) throws CrawlFailedException{
		if(projectName == null){
			throw new NullPointerException("The given project name is null."); 
		}
		
		String totalIssueNumInfoURL = getTotalIssueNumURL(projectName);
		String totalIssueNumInfo = CrawlerUtil.crawl(totalIssueNumInfoURL);
		
		//JSON format result: {"startAt":0,"maxResults":0,"total":6946,"issues":[]}
		JSONObject json = new JSONObject(totalIssueNumInfo);
		long totalIssueNum = json.getLong("total");
		return totalIssueNum;
	}

	/*
	 * Validate whether the expectedMaxResults is permitted or not for given project,
	 * if it is valid, return its value; otherwise, return the permitted maxResults. 
	 * 
	 * @param projectName: the given project name
	 * @param expectedMaxResults: the expected maxResults
	 * 
	 * @throws exceptions.CrawlFailedException
	 * 		If fails to crawl the content.
	 *
	 * @return: a valid maxResults, namely the minimum value of the expectedMaxResults and the permitted maxResults.
	 */	
	private static int validateMaxResults(String projectName,int expectedMaxResults) throws CrawlFailedException{
		if(projectName == null){
			throw new NullPointerException("The given project name is null");
		}
		
		//using default maxResults to crawl a piece of issue data
		String issueMetadataURL = getIssueMetadataURL(projectName, 0, expectedMaxResults);
		String issueMetadataAsJson = CrawlerUtil.crawl(issueMetadataURL);
		//JSON format result: {"expand":"names,schema","startAt":0,"maxResults":1,"total":6946,"issues":[{"expand":"operations,editmeta,changelog,transitions,renderedFields","id":"12929421","self":"https://issues.apache.org/jira/rest/api/2/issue/12929421","key":"LUCENE-6973"}]}
		JSONObject json = new JSONObject(issueMetadataAsJson);
		int maxResults = json.getInt("maxResults");

		return Math.min(maxResults, expectedMaxResults);
	}
	
	/*
	 * Get the URL which contains the information about total issue numbers of given project,
	 * the URL is similar to "https://issues.apache.org/jira/rest/api/2/search?jql=project=$projectName&maxResults=0"
	 * 
	 * @param projectName: project name
	 */
	private static String getTotalIssueNumURL(String projectName){
		String totalIssueNumURL = String.format(TOTAL_ISSUE_NUM_URL_TEMPLATE,projectName);
		return totalIssueNumURL;
	}
	
	/*
	 * Get the URL which contains the meta data of issues which begins at $startAt with at most maxResults issues
	 * the URL is similar to "https://issues.apache.org/jira/rest/api/2/search?jql=project=$projectName&startAt=$startAt&maxResults=$maxResults&fields=id,key".
	 * 
	 * @param projectName: project name
	 * @param startAt: the start index of issues
	 * @param maxResults: the max number of results
	 * 
	 */
	private static String getIssueMetadataURL(String projectName,int startAt,int maxResults){
		String issueMetadataURL = String.format(ISSUE_METADATA_URL_TEMPLATE,projectName,startAt,maxResults);
		return issueMetadataURL;
	}
	
	/*
	 * Get the URL of the issue which is identified by issue id
	 * the URL of an Apache issue, which uses JIRA issue system, 
	 *            is similar to "https://issues.apache.org/jira/rest/api/2/issue/$issueId?expand=changelog,renderedFields".
	 * 
	 * @param issueId: the identifier of an issue
	 */
	private static String getIssueURL(String issueId){
		String issueURL = String.format(ISSUE_URL_TEMPLATE,issueId);
		return issueURL;
	}
	
	/*
	 * Get the URL of the patch which is identified by patch id,
	 * the URL of an Apache patch is similar to "https://issues.apache.org/jira/secure/attachment/$patchId/$patchName".
	 * 
	 * @param patchId: the identifier of a patch
	 * @param patchName: the name of a patch, just for composing patch URL immediately.
	 */
	private static String getPatchURL(String patchId,String patchName){
		String patchURL = String.format(PATCH_URL_TEMPLATE, patchId,patchName);
		return patchURL;
	}
	
	public static void main(String[] args) throws Exception{
		String projectName = "httpclient";
		
		//test for method {method crawl}
		System.setOut(new PrintStream("d:/out.txt"));
		System.setErr(new PrintStream("d:/err.txt"));

		crawl(projectName);
		
		//test for method {@method getPatchURL}
//		String patchId = "12789915";
//		String patchName = "LUCENE-7048.patch";
//		String patchURL = getPatchURL(patchId,patchName);
//		String patchContent = CrawlerUtil.crawl(patchURL);
//		System.out.println(patchContent);
//		System.out.println("*********************************************");
//		
//		//test for method {@method getIssueURL}
//		String issueId = "12944003";
//		String issueURL = getIssueURL(issueId);
//		String issueContent = CrawlerUtil.crawl(issueURL);
//		System.out.println(issueContent);
//		System.out.println("*********************************************");
//		

//		//test for method {@method getTotalIssueNum}
//		long issueTotalNum = getTotalIssueNum(projectName);
//		System.out.println("issueTotalNum=" + issueTotalNum);
//
//		//test for method {@method validateMaxResults}
//		int maxResults = validateMaxResults(projectName, DEFAULT_MAX_RESULTS);
//		System.out.println("maxResults=" + maxResults);
//		maxResults = validateMaxResults(projectName, DEFAULT_MAX_RESULTS + 1);
//		System.out.println("maxResults=" + maxResults);
		
//		//test for method {@method getIssueMetadataURL}
//		int startAt = 0;
//		int maxResults = 100;
//		String issueMetadataURL = getIssueMetadataURL(projectName, startAt,maxResults);
//		System.out.println(issueMetadataURL);
//		String issueMetaData = CrawlerUtil.crawl(issueMetadataURL);
//		System.out.println(issueMetaData);
//		System.out.println("*********************************************");
//		
//		//test for method {@method getTotalIssueNumURL}
//		String totalIssueNumURL = getTotalIssueNumURL(projectName);
//		System.out.println(totalIssueNumURL);
//		String totalIssueNumContent = CrawlerUtil.crawl(totalIssueNumURL);
//		System.out.println(totalIssueNumContent);
//		System.out.println("*********************************************");
	}
}
