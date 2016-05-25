package crawlers.mail;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import crawlers.issuetracker.exception.CrawlFailedException;
import crawlers.issuetracker.util.CrawlerUtil;

public class TempExtractor {
	private Pattern mboxRe = Pattern.compile("\\d+.mbox");
	
	public Set<String> extractMboxInfo(String url){
		String htmlSource = null;
		
			try
			{
				htmlSource = CrawlerUtil.crawl(url);
			}
			catch (CrawlFailedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		if(htmlSource == null){
			return Collections.emptySet();
		}
		
		Set<String> mboxSet = new TreeSet<>();
		Matcher matcher = mboxRe.matcher(htmlSource);
		while(matcher.find()){
			String mbox = matcher.group();
			mboxSet.add(mbox);
		}

		return mboxSet;
	}
    
	public static void main(String[] args) throws Exception{
		TempExtractor extractor = new TempExtractor();
		String url = "http://mail-archives.apache.org/mod_mbox/lucene-java-user/";
		Set<String> mboxSet = extractor.extractMboxInfo(url);
		String dstFolderPath = "d:/mails";
		for (String mboxName : mboxSet) {
			String mboxURL = url + "/" + mboxName;
			System.out.println("Crawling " + mboxURL);
			try {
				FileUtils.copyURLToFile(new URL(mboxURL), new File(
						dstFolderPath + "/" + mboxName));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
