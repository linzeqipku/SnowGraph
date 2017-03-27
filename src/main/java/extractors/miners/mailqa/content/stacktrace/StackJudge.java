package extractors.miners.mailqa.content.stacktrace;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import extractors.miners.mailqa.entity.Segment;
import extractors.miners.mailqa.entity.Sentence;

public class StackJudge {

	public static boolean isStackTrace(Segment segment) {
		boolean result = false;

		ArrayList<Sentence> sentences = segment.getSentences();
		int totalLine = sentences.size();
		int atLine = 0;
		for (Sentence sentence : sentences) {
			String line = sentence.toString();
			if (isAtLine(line) || isEllipsisLine(line) || isCausedByLine(line)) {
				atLine++;
			}
		}

		if ((atLine > totalLine * 0.6))
			result = true;

		return result;
	}

	public static Segment shortStackTrace(Segment segment) {
		ArrayList<Sentence> shortStack = new ArrayList<Sentence>();
		ArrayList<Sentence> sentences = segment.getSentences();
		
		String exceptionMessage = "";
		int index = 0;
		while (index < sentences.size()) {
			if (isExceptionLine(sentences.get(index).toString())) {
				exceptionMessage = sentences.get(index).toString();
				break;
			}
			index++;
		}
		shortStack.add(new Sentence(exceptionMessage));

		if (exceptionMessage.equals(""))
			index = 0;

		
		while (index < sentences.size()) {
			String line = sentences.get(index).toString();
			if (isCausedByLine(line) || isEllipsisLine(line)) {
				shortStack.add(new Sentence(line));
			}
			index++;
		}

		segment.setSentences(shortStack);
		return segment;
	}

	public static boolean isExceptionLine(String line) {
		boolean result = false;

		line = line.trim().toLowerCase();
		if (line.indexOf("exception") != -1 || line.indexOf("error") != -1)
			result = true;

		return result;
	}

	public static boolean isAtLine(String line) {
		boolean result = false;

		if (line.trim().startsWith("at"))
			result = true;

		String reg = "^(at)?.*\\(.*\\)$";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(line.toLowerCase().trim());
		if (matcher.find())
			result = true;

		return result;
	}

	public static boolean isEllipsisLine(String line) {
		boolean result = false;

		String reg = ".*\\.{3}\\s*[1-9]*\\s*more$";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(line.toLowerCase().trim());
		if (matcher.find())
			result = true;

		return result;
	}

	public static boolean isCausedByLine(String line) {
		boolean result = false;

		if (line.toLowerCase().trim().startsWith("caused by:"))
			result = true;

		return result;
	}

	public static void main(String args[]) {
		String test = "Caused by: java.lang.ClassCastException: class";
		System.out.println(isCausedByLine(test));

	}
}
