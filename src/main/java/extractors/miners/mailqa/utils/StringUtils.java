package extractors.miners.mailqa.utils;

import java.io.StringReader;
import java.util.ArrayList;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.CoreLabelTokenFactory;

public class StringUtils {

	private static final int	MAX_LENGTH	= 10000;
	private static int			matrix[][]	= new int[MAX_LENGTH][MAX_LENGTH];

	/**
	 * @Title:StringUtils
	 * @Description: ��֤�ַ��СС��1000 ���Ƴ�ǰ���ͺ󵼿ո�
	 * @param str
	 * @return
	 */

	public static String trimString(String str) {
		String ret = "";
		if (str == null)
			return ret;
		ret = str.trim();
		if (ret.length() > MAX_LENGTH) {
			ret = ret.substring(0, MAX_LENGTH);
		}
		return ret.trim();
	}

	public static String legalFileName(String pre) {
		String words[] = pre.split("[\\s]+");
		pre = "";
		for (String word : words) {
			pre += word + "_";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pre.length(); i++) {
			char c = pre.charAt(i);
			if (c == '?' || c == '*' || c == '\\' || c == '/' || c == '<' || c == '>' || c == ':'
					|| c == '\'' || c == '"' || c == '|') {
				sb.append(' ');
			}
			else
				sb.append(c);
		}

		return sb.toString().length() >= 255 ? sb.toString().substring(0, 255) : sb.toString();
	}

	public static boolean isCommonSequence(String big, String small) {

		// String bigs[] = big.split("\\s+");
		// String smalls[] = small.split("\\s+");
		ArrayList<String> bigs = splitSentenceWithPunctuation(big);
		ArrayList<String> smalls = splitSentenceWithPunctuation(small);
		if (smalls.size() > bigs.size() || bigs.size() == 0)
			return false;

		for (int i = 0; i <= bigs.size(); i++)
			matrix[i][0] = 0;
		for (int j = 0; j <= smalls.size(); j++)
			matrix[0][j] = 0;

		for (int i = 1; i <= bigs.size(); i++) {
			for (int j = 1; j <= smalls.size(); j++) {
				if (bigs.get(i - 1).equals(smalls.get(j - 1))) {
					matrix[i][j] = matrix[i - 1][j - 1] + 1;
				}
				else {
					matrix[i][j] = Math.max(matrix[i - 1][j], matrix[i][j - 1]);
				}
			}
		}
		// System.out.println(matrix[bigs.length][smalls.length]);
		int temp = matrix[bigs.size()][smalls.size()];
		if (temp * 10 >= smalls.size() * 8)
			return true;
		return false;

	}

	public static ArrayList<String> splitSentenceWithPunctuation(String text) {
		PTBTokenizer ptbt = new PTBTokenizer(new StringReader(text), new CoreLabelTokenFactory(),
				"");
		ArrayList<String> result = new ArrayList<String>();
		for (CoreLabel label; ptbt.hasNext();) {
			label = (CoreLabel) ptbt.next();
			// System.out.println(label);
			result.add(label.toString());
		}

		return result;
	}

	public static void main(String args[]) {
		// String test = " adjsak djasldj";
		// System.out.println(StringUtils.trimString(test));

		String big = "What fields?";
		String small = "what ?";
		splitSentenceWithPunctuation(big);
		System.out.println(StringUtils.isCommonSequence(big.toLowerCase(), small.toLowerCase()));
	}
}
