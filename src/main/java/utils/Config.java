package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;


public class Config {
	private static Properties properties = new Properties();
	
	private static final String DEFAULT_LOG_PATH = "log/log.txt";
	
	private static final int DEFAULT_CONNECTION_TIMEOUT = 1000;//ms
	private static final int DEFAULT_READ_TIMEOUT = 1000;//ms
	private static final int DEFAULT_RETRY_TIMES = 3;
	
	private static final int DEFAULT_THREAD_POOL_SIZE = 10;
	
	/*
	 * Load key/value pairs from property file
	 */
	static{
		try(FileInputStream fis = new FileInputStream("src/config.properties")){
			properties.load(fis);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private static String getValue(String key){
		return properties.getProperty(key);
	}
	
	private static String getValue(String key,String defaultValue){
		String value = getValue(key);
		return (value != null ? value : defaultValue);
	}
	
	private static int getIntValue(String key, int defaultValue){
		String strValue = getValue(key);
		if(strValue == null){
			return defaultValue;
		}
		
		int nValue = defaultValue;
		try{
			nValue = Integer.parseInt(strValue);
		}catch(NumberFormatException e){//swallow NumberFormatException to handle with an invalid input
			e.printStackTrace();
		}
		return nValue;
	}
	
	
	public static String getLogPath(){
		return getValue("logPath",DEFAULT_LOG_PATH);
	}

	public static int getConnectionTimeout() {
		return getIntValue("connectiontimeout",DEFAULT_CONNECTION_TIMEOUT);
	}
	
	public static int getReadTimeout(){
		return getIntValue("readtimeout",DEFAULT_READ_TIMEOUT);
	}

	public static int getRetryTimes(){
		return getIntValue("retrytimes",DEFAULT_RETRY_TIMES);
	}
	
	public static int getThreadPoolSize(){
		return getIntValue("threadpoolsize",DEFAULT_THREAD_POOL_SIZE);
	}
	
	/* 
	 * Get issue file's saved path.
	 * Path format is: data/${projectName}/source_data/issue/${issueId}/${issueName}.json
	 */
	public static String getIssueFileSavedPath(String projectName, String issueId, String issueName){
		return String.format("data/%s/source_data/issue/%s/%s.json",projectName,issueId,issueName);
	}
	
	/*
	 * Get patch file's saved path.
	 * Path format is: data/${projectName}/source_data/issue/${issueId}/patches/${patchId}/${patchName} (patch name ends with ".patch")
	 */
	public static String getPatchFileSavedPath(String projectName,String issueId, String patchId, String patchName){
		return String.format("data/%s/source_data/issue/%s/patches/%s/%s", projectName,issueId,patchId,patchName);
	}
	
	public static void main(String[] args) throws Exception{
		//clear all key/value pairs in properties to test all default values
		properties.clear();
		assert getConnectionTimeout() == DEFAULT_CONNECTION_TIMEOUT;
		assert getReadTimeout() == DEFAULT_READ_TIMEOUT;
		assert getRetryTimes() == DEFAULT_RETRY_TIMES;
		assert getLogPath().equals(DEFAULT_LOG_PATH) == true;
		assert getThreadPoolSize() == DEFAULT_THREAD_POOL_SIZE;
		
		//custom configure file content
		int connTimeout = 100;
		int readTimeout = 200;
		int retryTimes = 1;
		int threadPoolSize = 5;
		String logPath = "logFilePath";
		StringBuilder configContentBuilder = new StringBuilder();
		configContentBuilder.append("connectiontimeout=").append(connTimeout).append("\n");
		configContentBuilder.append("readtimeout=").append(readTimeout).append("\n");
		configContentBuilder.append("retrytimes=").append(retryTimes).append("\n");
		configContentBuilder.append("logPath=").append(logPath).append("\n");
		configContentBuilder.append("threadpoolsize=").append(threadPoolSize).append("\n");
		
		properties.load(IOUtils.toInputStream(configContentBuilder.toString()));
		
		assert getConnectionTimeout() == connTimeout;
		assert getReadTimeout() == readTimeout;
		assert getRetryTimes() == retryTimes;
		assert getLogPath().equals(logPath) == true;
		assert getThreadPoolSize() == threadPoolSize;
		
		
		//test for method {$method getIssueFileSavedPath}
		String projectName = "lucene";
		String issueId = "2133";
		String issueName = "LUCENE-2133";		
		System.out.println(getIssueFileSavedPath(projectName, issueId, issueName));
		
		String patchId = "1234";
		String patchName = "LUCENE-2133.patch";
		System.out.println(getPatchFileSavedPath(projectName, issueId, patchId, patchName));
	}
}
