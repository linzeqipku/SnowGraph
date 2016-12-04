/**
 * @author Fang Lu, fanglupku@gmail.com 2011-4-13
 */
package extractors.miners.mailcode;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.StringTokenizer;

public class StringDealer {

	public static void main(String args[]) {
		String testString = "\n\npublic boolean compile()\n\n"
				+ "throws java.io.IOException\n\nabc\n\n";
		System.out.println("start:----");
		System.out.println(StringDealer.getLineNumber(StringDealer.trimEmptyLine(testString)));
		System.out.println("end:----");
	}

	public static String getStringForMaxLength(int maxLength, String origin) {
		if (origin == null) {
			return null;
		}
		else if (origin.length() <= maxLength) {
			return origin;
		}
		else {
			return new String(origin.substring(0, maxLength));
		}
	}

	public static int getLineNumber(String origin) {
		int result = 0;
		try {
			StringReader sr = new StringReader(origin);
			BufferedReader br = new BufferedReader(sr);
			while (br.readLine() != null) {
				++result;
			}
			br.close();
			sr.close();
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * erase empty line in start position and end position, like the
	 * String.trim, but this erase empty line
	 * 
	 * @return
	 */
	public static String trimEmptyLine(String origin) {
		StringBuffer sb = new StringBuffer();
		try {
			StringReader sr = new StringReader(origin);
			BufferedReader br = new BufferedReader(sr);
			String oneLine = br.readLine();
			boolean firstFlag = true;
			StringBuffer tempSB = new StringBuffer();
			while (oneLine != null) {
				if (firstFlag == true) {
					if (oneLine.trim().length() > 0) {
						sb.append(oneLine);
						sb.append("\n");
						firstFlag = false;
					}
					else {
					}
				}
				else {
					tempSB.append(oneLine);
					tempSB.append("\n");
					if (oneLine.trim().length() > 0) {
						sb.append(tempSB.toString());
						tempSB.delete(0, tempSB.length());
					}
					else {
					}
				}
				oneLine = br.readLine();
			}
			br.close();
			sr.close();
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * count the number of tokens cotained in a given string
	 * 
	 * @param src
	 * @return int
	 */
	public static int countTokens(String src) {
		if (src != null) {
			int count = 0;
			StringTokenizer tokenizer = new StringTokenizer(src);
			while (tokenizer.hasMoreTokens()) {
				tokenizer.nextToken();
				count++;
			}
			return count;
		}
		else {
			return 0;
		}
	}
}
