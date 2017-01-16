package extractors.miners.mailqa.utils;

import java.io.File;
import java.util.ArrayList;

/**
 * 
 * @Title: MBoxSpliter.java
 * 
 * @Prject: MBoxToSession
 * 
 * @Package: extractors.mi.mbox.utils
 * 
 * @Description: split MBox
 * 
 * @author: left
 * 
 * @date: 2013年9月24日 下午1:08:52
 * 
 * @version: V1.0
 */

public class MBoxSpliter {

	private static final String	pattern	= "From ";

	/**
	 * 
	 * @Title:MBoxSpliter
	 * 
	 * @Description: split mbox into some small msgs
	 * 
	 * @param mbox
	 *            a mbox file
	 * @return a list of String，include many msgs
	 */

	public static ArrayList<String> splitMBox(File mbox) {
		ArrayList<String> msgList = new ArrayList<String>();
		ArrayList<String> fileLines = ReadFile.readFileLines(mbox.getAbsolutePath());

		int preIndex = -1;
		int curIndex = -1;

		for (int i = 0; i < fileLines.size(); i++) {
			String line = fileLines.get(i);

			if (line.startsWith(pattern)) {
				if (preIndex == -1) {
					preIndex = i;
					continue;
				}
				curIndex = i;
				StringBuilder sb = new StringBuilder();
				for (int j = preIndex; j < curIndex; j++) {
					sb.append(fileLines.get(j) + "\r\n");
				}
				msgList.add(sb.toString());
				preIndex = curIndex;
			}
		}
		if (preIndex >= 0) {
			StringBuilder sb = new StringBuilder();
			for (int j = preIndex; j < fileLines.size(); j++) {
				sb.append(fileLines.get(j) + "\r\n");
			}
			msgList.add(sb.toString());
		}

		return msgList;
	}

	public static void main(String args[]) {
		File mbox = new File("D:\\lab\\邮件列表调研\\msg3.txt");
		ArrayList<String> msgList = MBoxSpliter.splitMBox(mbox);
		System.out.println(msgList.get(0));
	}
}
