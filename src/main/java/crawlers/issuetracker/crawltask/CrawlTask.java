package crawlers.issuetracker.crawltask;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;

import crawlers.issuetracker.util.CrawlerUtil;

/*
 * Encapsulate a crawling task to make it concurrent;
 * it saves crawled content to target file and 
 *    provides an interface to let subclasses handle crawled content for reducing memory usage. 
 */
public abstract class CrawlTask<T> implements Callable<T>{		
	private String strURL;
	private String fileSavedPath;
	
	public CrawlTask(String strURL,String fileSavedPath){
		this.strURL = strURL;
		this.fileSavedPath = fileSavedPath;
	}

	@Override
	public T call() throws Exception {
		if(strURL == null){
			throw new NullPointerException("URL or target file path is null.");
		}
		
		String content = CrawlerUtil.crawl(strURL);
		
		//save crawled content to target file if fileSavedPath is not null
		if(fileSavedPath != null){
			FileUtils.writeStringToFile(new File(fileSavedPath), content);	
		}
		
		//handle with crawled content, and return processed result.
		T result = handle(content);
		return result;
	}
	
	protected abstract T handle(String content);
}