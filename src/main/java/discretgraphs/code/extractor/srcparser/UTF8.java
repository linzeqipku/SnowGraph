package discretgraphs.code.extractor.srcparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class UTF8 {

	public static boolean isUTF8(byte[] bytes) {
		int UTF8cnt = 0;
		int error = 0;
		int offset = bytes.length;
		for (int i = 0; i < offset; i++) {
			if ((bytes[i] & (byte) 0x80) != 0) {
				if (i + 1 < offset && -64 <= bytes[i] && bytes[i] <= -33 && -128 <= bytes[i + 1]
						&& bytes[i + 1] <= -65) {
					i++;
					UTF8cnt += 2;
				}
				else if (i + 2 < offset && -32 <= bytes[i] && bytes[i] <= -17
						&& -128 <= bytes[i + 1] && bytes[i + 1] <= -65 && -128 <= bytes[i + 2]
						&& bytes[i + 2] <= -65) {
					i += 2;
					UTF8cnt += 3;
				}
				else
					error++;
			}
		}
		if ((double) UTF8cnt / (UTF8cnt + error) > 0.98)
			return true;
		return false;
	}

	public static String getContent(String path) {
		File file = new File(path);
		String charset = null;
		InputStream is = null;
		try {
			is = new FileInputStream(file);
		}
		catch (FileNotFoundException e) {
		}
		byte[] bytes = new byte[(int) file.length() + 10];
		int offset = 0;
		int numRead = 0;
		try {
			while ((offset < bytes.length)
					&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}
			is.close();
		}
		catch (Exception e) {
		}
		if (isUTF8(bytes))
			charset = "UTF-8";
		else
			charset = "GBK";
		FileInputStream fInputStream;
		InputStreamReader isr = null;
		try {
			fInputStream = new FileInputStream(file);
			isr = new InputStreamReader(fInputStream, charset);
		}
		catch (Exception e) {
		}
		StringBuffer str = new StringBuffer("");
		String tmp;
		BufferedReader in = new BufferedReader(isr);
		try {
			while ((tmp = in.readLine()) != null) {
				str.append(tmp + "\n");
			}
		}
		catch (Exception e) {
		}
		return str.toString();

	}

	public static void main(String args[]) {
		System.out.println(getContent("C:\\01工作库\\04实现\\01源程序\\00-通用架构\\ctz-portal-commons\\src\\com\\digitalchina\\frame\\utils\\XDOUtils.java"));
	}
}
