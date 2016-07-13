package pfr.plugins.parsers.javacode.extractor.srcparser;

import java.io.File;
import java.util.List;


/**
 * sei.pku.edu.cn
 * 
 * @author Jin Jing 2012-3-25 下午04:51:39
 * @version 0.1 2012-12-15
 */
public class JavaReverser {
	/**
	 * @param projectDir
	 * @param projectName
	 * @param projectDomain
	 * @return ElementInfoPool 逆向解析得到的所有信息存储在一个ElementInfoPool对象中
	 */
	public static ElementInfoPool reverse(String projectDir, String projectName) {	
		List<File> files = FileLoader
				.loadJavaFiles(projectDir);

		ElementInfoPool elementInfoPool = new ElementInfoPool(
				projectDir, projectName);

		JavaParser.parse(elementInfoPool, files);
		return elementInfoPool;
	}
	
	public static void main(String[] args) {
		String projectName = "POI";
		String sourcePath = "E:\\src\\poi";
		JavaReverser.reverse(sourcePath, projectName);
	}

}
