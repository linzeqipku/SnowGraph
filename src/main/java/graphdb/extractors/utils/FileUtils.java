package graphdb.extractors.utils;

import java.io.*;

public class FileUtils {

	public static String getFileContent(File file) {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
		}
		byte[] bytes = new byte[(int) file.length() + 10];
		int offset = 0;
		int numRead;
		try {
			while ((offset < bytes.length) && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}
			is.close();
		} catch (Exception e) {
		}
		FileInputStream fInputStream;
		InputStreamReader isr = null;
		try {
			fInputStream = new FileInputStream(file);
			isr = new InputStreamReader(fInputStream, "UTF-8");
		} catch (Exception e) {
		}
		StringBuffer str = new StringBuffer("");
		String tmp;
		BufferedReader in = new BufferedReader(isr);
		try {
			while ((tmp = in.readLine()) != null) {
				str.append(tmp + "\n");
			}
		} catch (Exception e) {
		}
		return str.toString();
	}

	public static void writeFile(File file, String content) {
		try {
			PrintStream ps = new PrintStream(file);
			ps.println(content);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void deleteDirectory(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) deleteDirectory(file);
			else file.delete();
		}
		dir.delete();
	}
}
