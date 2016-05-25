package crawlers.issuetracker.crawltask;

import java.io.File;

import utils.Config;

/*
 * Crawl patch, then save it to target file,
 * finally handle with the patch data (No any operation now)
 */
public class PatchCrawlTask extends CrawlTask<Void> {
	
	public PatchCrawlTask(String strURL, String fileSavedPath) {
		super(strURL, fileSavedPath);
	}

	@Override
	protected Void handle(String content) {
		return null;
	}

	public static void main(String[] args) throws Exception{
		String projectName = "lucene";
		String issueId = "12945265";
		String patchId = "12790354";
		String patchName = "LUCENE-7053.patch";
		
		String patchSavedPath = Config.getPatchFileSavedPath(projectName, issueId, patchId, patchName);
		String patchURL = "https://issues.apache.org/jira/secure/attachment/" + patchId + "/" + patchName;
		CrawlTask<Void> patchTask = new PatchCrawlTask(patchURL,patchSavedPath);
		patchTask.call();
		
		assert new File(Config.getPatchFileSavedPath(projectName, issueId, patchId, patchName)).isFile();
	}
}
