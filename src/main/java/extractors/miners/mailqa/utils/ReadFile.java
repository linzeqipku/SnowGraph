package extractors.miners.mailqa.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * @Title: ReadFile.java
 * @Package cn.edu.pku.EOS.crawler.util.mbox
 * @Description: ��ȡ�ļ��Ĺ�����
 * @author jinyong jinyonghorse@hotmail.com
 * @date 2013-5-25 ����12:35:55
 */

public class ReadFile {

	private static Logger	logger	= Logger.getLogger(ReadFile.class.getName());

	@SuppressWarnings("unchecked")
	public static ArrayList<String> readFileLines(String file_name) {
		ArrayList<String> fileLines = new ArrayList<String>();
		try {
			fileLines = (ArrayList<String>) FileUtils.readLines(new File(file_name));
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// File file = new File(file_name);
		// try {
		// br = new BufferedReader(new FileReader(file));
		// for(String line = br.readLine(); line != null; line = br.readLine())
		// {
		// fileLines.add(line);
		// }
		// } catch (FileNotFoundException e) {
		// System.out.println(new StringBuilder().append("File Not Found: ")
		// .append(file_name).toString());
		// } catch(IOException e) {
		// e.printStackTrace();
		// }
		return fileLines;
	}

	public static String read_file_string(String file_name) {
		File file = new File(file_name);
		FileReader fis = null;
		BufferedReader bis = null;
		String res = "";

		try {
			fis = new FileReader(file);
			bis = new BufferedReader(fis);
			int len = 10;

			char[] buff = new char[(int) Math.max(len, file.length())];

			while (bis.ready()) {
				len = bis.read(buff);
				res = new StringBuilder().append(res).append(new String(buff)).toString();
			}
			fis.close();
			bis.close();
		}
		catch (FileNotFoundException e) {
			System.out.println(new StringBuilder().append("File Not Found: ").append(file_name)
					.toString());
		}
		catch (IOException e) {
			System.out.println(e);
		}
		return res;
	}

	public static StringBuilder read_file_builder(String file_name) {
		File file = new File(file_name);
		FileReader fis = null;
		BufferedReader bis = null;
		StringBuilder res = new StringBuilder();
		try {
			fis = new FileReader(file);
			bis = new BufferedReader(fis);
			int len = 10;
			char[] buff = new char[(int) Math.max(len, file.length())];
			while (bis.ready()) {
				len = bis.read(buff);
				res.append(buff, 0, len);
			}
			fis.close();
			bis.close();
		}
		catch (FileNotFoundException e) {
			System.out.println(new StringBuilder().append("File Not Found: ").append(file_name)
					.toString());
		}
		catch (IOException e) {
			System.out.println(e);
		}
		return res;
	}

	public static String read_file(String file_name) {
		return read_file_string(file_name);
	}

	public static byte[] getBytpesFromFile(String file_name) throws IOException {
		File file = new File(file_name);
		byte[] bytes = null;
		bytes = getBytesFromFile(file);
		return bytes;
	}

	@SuppressWarnings("resource")
	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		long length = file.length();
		byte[] bytes = new byte[(int) length];
		int offset = 0;
		int numRead = 0;

		while ((offset < bytes.length)
				&& ((numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)) {
			offset += numRead;
		}

		if (offset < bytes.length) {
			throw new IOException(new StringBuilder().append("Could not completely read file ")
					.append(file.getName()).toString());
		}
		is.close();
		return bytes;
	}

	public static void main(String args[]) {
		File file = new File("D:/lab/�ʼ��б����/msg2.txt");
		ArrayList<String> lines = ReadFile.readFileLines(file.getAbsolutePath());
		for (String str : lines) {
			logger.info(str);
		}
	}
}