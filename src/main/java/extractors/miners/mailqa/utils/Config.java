package extractors.miners.mailqa.utils;

import java.util.ResourceBundle;

public class Config {

	private static String			CONFIG_FILE_NAME	= "config";
	private static ResourceBundle	bundle;

	static {
		try {
			bundle = ResourceBundle.getBundle(CONFIG_FILE_NAME);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getValue(String key) {
		return bundle.getString(key);
	}

	public static String getDbUsr() {
		//return getValue("dbusr");
		return "root";
	}

	public static String getDbUrl() {
		//return getValue("dburl");
		return "jdbc:mysql://localhost:3306/efaq";
	}

	public static String getDbPwd() {
		//return getValue("dbpwd");
		return "root";
	}

	public static String getLuceneMboxUrl() {
		return getValue("lucene_mbox_url");
	}

	public static String getTomcatMboxUrl() {
		return getValue("tomcat_mbox_url");
	}

	public static String getLuceneSrcUrl() {
		return getValue("lucene_src_url");
	}

	public static String getTomcatSrcUrl() {
		return getValue("tomcat_src_url");
	}

	public static String getLuceneMboxFilePath() {
		return getValue("lucene_mbox_file_path");
	}

	public static String getProjectId() {
		return getValue("project_id");
	}

	public static String getTomcatMboxFilePath() {
		return getValue("tomcat_mbox_file_path");
	}

	public static String getLuceneSessionContentPath() {
		return getValue("lucene_session_path");
	}

	public static String getTomcatSessionContentPath() {
		return getValue("tomcat_session_path");
	}

	public static void main(String args[]) {
		System.out.println("dbusr is: " + getDbUsr());
		System.out.println("dburl is: " + getDbUrl());
		System.out.println("dbpwd is: " + getDbPwd());
		System.out.println("project id is: " + getProjectId());
		System.out.println("lucene mbox url is: " + getLuceneMboxUrl());
		System.out.println("tomcat mbox url is: " + getTomcatMboxUrl());
		System.out.println("lucene src url is: " + getLuceneSrcUrl());
		System.out.println("tomcat src url is: " + getTomcatSrcUrl());
		System.out.println("lucene mbox file path is: " + getLuceneMboxFilePath());
		System.out.println("tomcat mbox file path is :" + getTomcatMboxFilePath());
		System.out.println("lucene session content file path is: " + getLuceneSessionContentPath());
		System.out.println("tomcat session content file path is :" + getTomcatSessionContentPath());
	}
}
