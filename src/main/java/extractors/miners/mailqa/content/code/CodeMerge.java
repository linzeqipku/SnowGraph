package extractors.miners.mailqa.content.code;

/**
 * @ClassName: CodeMerge
 * @Description: TODO 合并相邻的代码段落
 * @author: left
 * @date: 2014年3月5日 下午3:34:20
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import extractors.miners.mailqa.entity.Segment;
import extractors.miners.mailqa.entity.Sentence;

/**
 * @author Fang Lu, fanglupku@gmail.com
 */

public class CodeMerge {

	// public static void main(String args[]){
	// }
	public static final int		NO_CODE					= 0;
	public static final int		BEGIN_CODE				= 1 << 1;
	public static final int		END_CODE				= 1 << 2;
	public static final int		BEGIN_COMMENT			= 1 << 3;
	public static final int		END_COMMENT				= 1 << 4;
	public static final int		TOTAL_CODE				= 1 << 5;
	public static final int		EMPTY_ITEM				= 1 << 6;
	// public static final int INCLUDE_CODE = 1 << 3;

	private static final int	TEXT_TOKEN_THRESHOLD	= 22;

	public static boolean simpleJudgeCode(String line) {
		line = line.trim();
		boolean isCode = false;
		StringTokenizer st = new StringTokenizer(line, " \t\n\r\f(){}[];=+-*/&!~");
		for (String s : CodeJudge.mustOccurSymbol) {
			if (line.contains(s)) {
				return true;
			}
		}
		for (String s : CodeJudge.codeEndSymbol) {
			if (line.endsWith(s)) {
				return true;
			}
		}
		while (st.hasMoreTokens()) {
			String thisToken = st.nextToken();
			for (String s : CodeJudge.codeKeywordSymbol) {
				if (thisToken.equals(s)) {
					return true;
				}
			}
		}

		return isCode;
	}

	/**
	 * @Title:CodeMerge
	 * @Description:delete text's comment
	 * @param text
	 * @return
	 */

	public static String eraseComment(String text) {
		String strPattern = "/\\u002A.*?\\u002A/";
		Pattern pattern = Pattern.compile(strPattern, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(text);
		String result = matcher.replaceAll("");
		strPattern = "//.*";
		pattern = Pattern.compile(strPattern);
		matcher = pattern.matcher(result);
		result = matcher.replaceAll("");
		return result;
	}

	public static int computeCodeStatu(String text) {
		int result = NO_CODE;
		boolean isFirstLine = true;
		int codeLine = 0;
		int totalLine = 0;
		int tokenCount = 0;
		int speratorCount = 0;
		String textNoComment = eraseComment(text);
		for (int i = 0; i < textNoComment.length(); ++i) {
			if (textNoComment.charAt(i) == ';' || textNoComment.charAt(i) == '}'
					|| textNoComment.charAt(i) == '{' || textNoComment.charAt(i) == '('
					|| textNoComment.charAt(i) == ')' || textNoComment.charAt(i) == '=') {
				++speratorCount;
			}
		}
		codeLine = StringDealer.getLineNumber(text) - StringDealer.getLineNumber(textNoComment);
		StringReader sr = new StringReader(textNoComment);
		BufferedReader br = new BufferedReader(sr);

		try {
			String line = br.readLine();
			boolean isCode = false;
			if (textNoComment.length() != text.length()) {
				isCode = true;
			}
			while (line != null) {
				line = line.trim();
				if (line.length() > 0) {
					isCode = simpleJudgeCode(line);
					++totalLine;
					if (isCode) {
						// result = result | INCLUDE_CODE;
						++codeLine;
					}
					if (isFirstLine && isCode) {
						result = result | BEGIN_CODE;
					}
					isFirstLine = false;
				}
				tokenCount += StringDealer.countTokens(line);
				line = br.readLine();
			}
			double codeRatio = (double) codeLine / (double) totalLine;
			if (codeRatio > 0.5) {
				result = result | TOTAL_CODE;
			}
			if (isCode) {
				result = result | END_CODE;
				if (isFirstLine == true) {
					result = result | BEGIN_CODE;
				}
			}
			if (codeLine == 1) {
				if (speratorCount == 0 || (tokenCount) / speratorCount > TEXT_TOKEN_THRESHOLD) {
					result = NO_CODE;
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * @Title:CodeMerge
	 * @Description: judge whether a continual code
	 * @param segFirst
	 * @param segSecond
	 * @return
	 */

	public static boolean isContinualCode(Segment segFirst, Segment segSecond) {
		int firstStatu = computeCodeStatu(segFirst.getContentText());
		int secondStatu = computeCodeStatu(segSecond.getContentText());
		// System.out.println("++:" + firstStatu + "/" + secondStatu);
		if ((firstStatu & END_CODE) != 0 && (secondStatu & BEGIN_CODE) != 0) {
			// && (firstStatu & TOTAL_CODE) != 0 && (secondStatu & TOTAL_CODE
			// )!= 0){
			return true;
		}
		return false;
	}

	public static boolean isEmptyString(String input) {
		if (input == null || input.replaceAll("\\s", "").trim().length() == 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public static int computeCommentStat(Segment thisItem) {
		String content = thisItem.getContentText();
		if (content == null || content.replaceAll("\\s", "").trim().length() == 0) {
			return EMPTY_ITEM;
		}
		int beginIndex = 0;
		int endIndex = 0;
		int result = 0;
		while ((beginIndex = content.indexOf("/*", endIndex)) > -1) {
			beginIndex += 2;
			result = result | BEGIN_COMMENT;
			if ((endIndex = content.indexOf("*/", beginIndex)) > -1) {
				result = result | END_COMMENT;
				endIndex += 2;
			}
			else {
				result = result & (~END_COMMENT);
				break;
			}
		}
		if ((result & BEGIN_COMMENT) == 0) {
			if ((endIndex = content.indexOf("*/")) > -1) {
				result = result | END_COMMENT;
			}
		}
		return result;
	}

	public ArrayList<Segment> continualCommentMerge(ArrayList<Segment> srcList) {
		ArrayList<Segment> result = new ArrayList<Segment>();
		int srcIndex = 0;
		for (srcIndex = 0; srcIndex < srcList.size();) {
			int thisCommentStatu = computeCommentStat(srcList.get(srcIndex));
			if (((thisCommentStatu & BEGIN_COMMENT) != 0)
					&& ((thisCommentStatu & END_COMMENT) == 0)) {
				int beginIdex = srcIndex;
				// int endIndex = srcIndex;
				StringBuffer sb = new StringBuffer();
				sb.append(srcList.get(srcIndex).getContentText());
				++srcIndex;
				int ignoreEmptyCount = 0;
				while (srcIndex < srcList.size()) {
					thisCommentStatu = computeCommentStat(srcList.get(srcIndex));
					if ((thisCommentStatu & EMPTY_ITEM) != 0) {
						++srcIndex;
						++ignoreEmptyCount;
						continue;
					}
					ignoreEmptyCount = 0;
					sb.append("\n");
					sb.append(srcList.get(srcIndex).getContentText());
					if ((thisCommentStatu & END_COMMENT) != 0) {
						break;
					}
					++srcIndex;
				}
				srcIndex = srcIndex - ignoreEmptyCount;
				if (srcIndex >= srcList.size()) {
					srcIndex = srcList.size() - 1;
				}
				Segment newItem = new Segment();
				ArrayList<Sentence> sentences = new ArrayList<Sentence>();
				sentences.add(new Sentence(sb.toString()));
				newItem.setSentences(sentences);
				newItem.setContentType(Segment.CODE_CONTENT);
				result.add(newItem);
				++srcIndex;
			}
			else {
				result.add(srcList.get(srcIndex));
				++srcIndex;
			}
		}
		return result;
	}

	public static ArrayList<Segment> continualCodeMerge(ArrayList<Segment> srcList) {
		// ArrayList<Segment> continualCommentList =
		// continualCommentMerge(srcList);
		ArrayList<Segment> result = new ArrayList<Segment>();

		int preIndex = 0, currentIndex = 1;

		while (currentIndex < srcList.size()) {
			int preType = srcList.get(preIndex).getContentType();
			int currentType = srcList.get(currentIndex).getContentType();

			if (preType != Segment.CODE_CONTENT) {
				result.add(srcList.get(preIndex));
				preIndex++;
				currentIndex++;
				continue;
			}

			while (currentType == Segment.CODE_CONTENT && currentIndex < srcList.size()) {
				currentIndex++;
			}

			
			ArrayList<Sentence> sentences = new ArrayList<Sentence>();
			Segment newItem = new Segment();
			for (int i = preIndex; i < currentIndex; i++) {
				sentences.addAll(srcList.get(i).getSentences());
			}
			newItem.setSentences(sentences);
			newItem.setContentType(Segment.CODE_CONTENT);
			result.add(newItem);
			preIndex = currentIndex;
			currentIndex++;
		}
		if(srcList.size() > 0)
			result.add(srcList.get(srcList.size()-1));
		return result;
	}
}
