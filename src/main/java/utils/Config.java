package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
	private static Properties properties = new Properties();

	/*
	 * Load key/value pairs from property file
	 */
	static{
		
		try(InputStream is = Config.class.getClassLoader().getResourceAsStream("config.properties")){
			properties.load(is);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private static String getValue(String key){
		return properties.getProperty(key);
	}
	
	public static String getValue(String key,String defaultValue){
		String value = getValue(key);
		return (value != null ? value : defaultValue);
	}
	
	public static int getIntValue(String key, int defaultValue){
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
		System.out.println(Config.getIntValue("connectiontimeout", 0));
	}
}
