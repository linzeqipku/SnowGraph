/**
 * File-Name:FileUtils.java
 * 
 * Created on 2011-7-16 上午07:20:07
 * 
 * @author: Neo (neolimeng@gmail.com) Software Engineering Institute, Peking
 *          University, China
 * 
 *          Copyright (c) 2009, Peking University
 * 
 * 
 */
package extractors.miners.mailqa.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Description:
 * 
 * @author: Neo (neolimeng@gmail.com) Software Engineering Institute, Peking
 *          University, China
 * @version 1.0 2011-7-16 上午07:20:07
 */
public class FileUtils {

	public static void write(String savePath, String fileName, File file) {
		try {
			FileOutputStream fos = new FileOutputStream(savePath + getValidFileName(fileName));
			System.out.println(savePath + getValidFileName(fileName));
			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getExtention(String fileName) {
		int pos = fileName.lastIndexOf(".");
		return fileName.substring(pos);
	}

	public static String getValidFileName(String originalFileName) {
		return originalFileName.replace("!,@,#,$,%,^,&,*,~,", "_");
	}

	/**
	 * Description:
	 * 
	 * @param args
	 *            void
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
