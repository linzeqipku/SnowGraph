/**
 * 
 */
package pfr.plugins.parsers.javacode.extractor.srcparser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * sei.pku.edu.cn
 *
 * @author Jin Jing 2012-10-16 下午11:19:24
 */
public class Config {

	public static final String JDBC_URL = "JdbcUrl";
	
	public static final String JDBC_USER = "JdbcUser";
	
	public static final String JDBC_PWD = "JdbcPwd";
	
	public static final String JAVA_LANG_FILTER_FILE = "JavaLangFilterFile";
	
	public static final String COMMON_WORD_FILTER_FILE = "CommonWordFilterFile";
	
	public static final String IS_STEMMING = "IsStemming";
	
	public static final String JAVA_LANG_PACKAGE = "JavaLangPackage";
	
	public static final String LDA_PROCESS_FOLDER = "LDAProcessFolder";
	
	public static final String PROJECT_STORE_PATH = "ProjectStorePath";
	
	public static final String TOPIC_SPLIT_NUM = "TopicSplitNum";
	
	private Properties properties = new Properties();
	
	private static final String CONFIG_FILE = "config.properties";
	
	private static Config config = new Config();
	
	private Config() {
		InputStream configFile = Config.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
		try {
			properties.load(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProperty(String name) {
		return config.properties.getProperty(name);
	}
}
