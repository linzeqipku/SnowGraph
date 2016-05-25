package crawlers.issuetracker.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import crawlers.issuetracker.exception.CrawlFailedException;
import utils.Config;

public class CrawlerUtil {
	private static final Charset CHARSET = StandardCharsets.UTF_8;
	
	private static int connectionTimeout = 5000;//ms, namely 5s in default
	private static int readTimeout = 5000;//ms, namely 5s in default
	private static int retryTimes = 3;//retry three times after failing to crawl a web page in default

	/*
	 * Initialize Crawler
	 */
	static{
		connectionTimeout = Config.getConnectionTimeout();
		readTimeout = Config.getReadTimeout();
		retryTimes = Config.getRetryTimes();
	}
	
	/*
	 * Crawl a web page, then return its content.
	 * 
	 * @param strURL: the given web page url
	 * 
	 * @throws exceptions.CrawlFailedException
	 * 		If crawling fails after retrying.
	 * 
	 * @return: the content of given url
	 */
	public static String crawl(String strURL) throws CrawlFailedException{
		if(strURL == null){
			throw new NullPointerException("The URL of crawling task is null.");
		}
		
		return crawlAtMostRetryTimes(strURL);
	}
	
	/*
	 * Crawl a web page, then save its content to given target file.
	 * 
	 * @param strURL: the given web page url
	 * @param target: the target file
	 * 
	 * @throws exceptions.CrawlFailedException
	 * 		If crawling fails after retrying.
	 * 
	 * @return: TRUE if saving content from given url to target file successfully, 
	 *          otherwise, FALSE will be returned.
	 */
	public static boolean crawlToFile(String strURL,File target) throws CrawlFailedException{
		if(strURL == null || target == null){
			throw new NullPointerException("Crawling URL or target file is null.");
		}
		
		String content = crawlAtMostRetryTimes(strURL);
		if(content == null){
			return false;
		}else{
			try {
				FileUtils.writeStringToFile(target,content,CHARSET);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	
	/*
	 * Crawl a web page, then return its content. 
	 * If any exception exists, it will retry to crawl again and again, at most $retryTimes tries.
	 * 
	 * @param strURL: the given web page url
	 * 
	 * @throws exceptions.CrawlFailedException
	 * 		If crawling fails after retrying.
	 * 
	 * @return: the content of given url
	 */
	private static String crawlAtMostRetryTimes(String strURL) throws CrawlFailedException{
		Pair<Integer,String> crawlResPair = null;
		
		//One crawl and at most {@field retryTimes} retries
		for(int i=0;i<=retryTimes;i++){
			try {
				crawlResPair = crawl0(strURL);
			} catch (IOException e) {
				System.err.println(e.getMessage() + "[" + (i+1) + "th crawling exception]");
			}
			
			if(crawlResPair == null){
				continue;
			}
			
			int status = crawlResPair.getLeft();
			String content = crawlResPair.getRight();
			
			if(status == HttpURLConnection.HTTP_OK){
				if(content != null){//return crawled content immediately
					return content;
				}else{
					continue;
				}
			}else if(status == HttpURLConnection.HTTP_NOT_FOUND){//white list to return null directly.
				return null;
			}else{
				continue;
			}
		}
				
		throw new CrawlFailedException("Cralwing " + strURL + " failed after " + retryTimes + " retries.");
	}
	
	/*
	 * Crawl a web page, then return the pair of the status code and content of response. 
	 * 
	 * @param strURL: the given web page url
	 * 
	 * @throws java.io.IOException
	 * 		If I/O operation fails or be interrupted.
	 * 
	 * @return: the pair of the status code and content of response (status code, content)
	 */
	private static Pair<Integer,String> crawl0(String strURL) throws IOException{		
		StringBuilder resultBuilder = new StringBuilder();
	
		HttpURLConnection conn = (HttpURLConnection)new URL(strURL).openConnection();
		conn.setConnectTimeout(connectionTimeout);
		conn.setReadTimeout(readTimeout);
		
		String content = null;
		int statusCode = conn.getResponseCode();
		
		if(statusCode == HttpURLConnection.HTTP_OK){
			//read data to resultBuilder
			InputStream is = conn.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,CHARSET));
			String line = null;
			while((line = reader.readLine()) != null){
				resultBuilder.append(line).append("\n");
			}
			
			//close the input stream
			is.close();
			
			content = resultBuilder.toString();	
		}else{ 
			content = null;
		}
		
		return Pair.of(statusCode, content);
	}

	public static void main(String[] args) throws Exception {
		String strURL = "https://issues.apache.org/jira/rest/api/2/search?jql=project=LUCENE&startAt=0&maxResults=100&fields=id,key";
		System.out.println(crawl(strURL));
	}

	/*
	 * Set Crawler's connection timeout in ms.
	 */
	public static void setConnectionTimeout(int _connectionTimeout){
		connectionTimeout = _connectionTimeout;
	}
	
	/*
	 * Set Crawler's read timeout in ms.
	 */
	public static void setReadTimeout(int _readTimeout){
		readTimeout = _readTimeout;
	}
	
	/*
	 * set retry times after failing to crawl a web page
	 */
	public static void setRetryTimes(int _retryTimes){
		retryTimes = _retryTimes;
	}
}
