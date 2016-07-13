/**
 * 
 */
package pfr.plugins.parsers.javacode.extractor.srcparser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;




/**
 * 
 * sei.pku.edu.cn
 *
 * @author Jin Jing 2012-12-21 上午12:46:33
 * @version 0.1 2012-12-21
 */
public class JavaLangPackage {
	private static HashSet<String> simpleTypeSet = new HashSet<String>();
	
	private static List<String> qualifiedTypeList = new ArrayList<String>();
	
	@SuppressWarnings("unchecked")
	public static void initial() {
		List<String> classes = null;
		try {
			classes = FileUtils.readLines(new File(Config.getProperty(Config.JAVA_LANG_PACKAGE)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(classes != null) {
			qualifiedTypeList.addAll(classes);
			for(String s : classes) {
				simpleTypeSet.add(s.substring(s.lastIndexOf(".") + 1));
			}
		}
	}
	
	public static boolean isJavaLangType(String simpleType) {
		if(simpleTypeSet.contains(simpleType)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static String findQualifiedType(String simpleType) {
		if(simpleTypeSet.contains(simpleType)) {
			for(String qualifiedType : qualifiedTypeList) {
				if(qualifiedType.endsWith(simpleType))
					return qualifiedType;
			}
			return null;
		}
		else {
			return null;
		}
	}
	
}
