/** 
 * File-Name:ProjectPreprocessor.java
 *
 * Created on 2012-3-23 下午7:47:05
 * 
 * @author: Neo (neolimeng@gmail.com)
 * Software Engineering Institute, Peking University, China
 * 
 * Copyright (c) 2009, Peking University
 * 
 *
 */
package discretgraphs.code.extractor.srcparser;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
//import org.apache.log4j.Logger;

/**
 * Description:
 * 
 * @author: Neo (neolimeng@gmail.com) Software Engineering Institute, Peking
 *          University, China
 *          
 *          Jin Jing
 *          
 * @version 1.0 2012-3-23 下午7:47:05
 */
public class FileLoader {
	//private static Logger log = Logger.getLogger(ProjectPreprocessor.class);

	/**
	 * 
	 * Description:Load all Java source code files form the project src
	 * directory.
	 * 
	 * @author: Neo (neolimeng@gmail.com) Software Engineering Institute, Peking
	 *          University, China
	 * @version 1.0 2012-3-23 下午7:49:21
	 * @param projectSrcPath
	 * @return List<File>
	 */
	public static List<File> loadJavaFiles(String projectSrcPath) {
		if(projectSrcPath.endsWith("java")) {
			List<File> javaFiles = new ArrayList<File>();
			javaFiles.add(new File(projectSrcPath));
			return javaFiles;
		}
		try {
			File dirs = new File(projectSrcPath);
			String dirPath = dirs.getCanonicalPath();
			// log.debug("Loading Java files from src path: " + dirPath);
			File root = new File(dirPath);

			List<File> javaFiles = new ArrayList<File>();

			@SuppressWarnings("unchecked")
			Iterator<File> iterator = FileUtils.iterateFiles(root,
					new String[] { "java" }, true);
			while (iterator.hasNext()) {
				File file = iterator.next();
				// log.debug(file.getName());
				javaFiles.add(file);
			}

			// log.debug("Total " + javaFiles.size() + " Java files found.");

			return javaFiles;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * JarPath的子目录下的所有jar包都会被解析
	 */
	public static List<File> loadJarFiles(String jarPath) {
		if(jarPath.endsWith("jar")) {
			List<File> jarFiles = new ArrayList<File>();
			jarFiles.add(new File(jarPath));
			return jarFiles;
		}
		try {
			File dirs = new File(jarPath);
			String dirPath = dirs.getCanonicalPath();
			// log.debug("Loading Java files from src path: " + dirPath);
			File root = new File(dirPath);

			List<File> jarFiles = new ArrayList<File>();

			@SuppressWarnings("unchecked")
			Iterator<File> iterator = FileUtils.iterateFiles(root,
					new String[] { "jar" }, true);
			while (iterator.hasNext()) {
				File file = iterator.next();
				// log.debug(file.getName());
				jarFiles.add(file);
			}

			// log.debug("Total " + javaFiles.size() + " Java files found.");

			return jarFiles;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
