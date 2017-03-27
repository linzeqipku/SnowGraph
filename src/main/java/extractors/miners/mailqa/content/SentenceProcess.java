package extractors.miners.mailqa.content;

import java.util.ArrayList;

//import extractors.miners.mailqa.dao.MessageDao;
import extractors.miners.mailqa.entity.Email;
import extractors.miners.mailqa.entity.Segment;
import extractors.miners.mailqa.entity.Sentence;
import extractors.miners.mailqa.utils.ReadFile;

/**
 * @ClassName: SentenceProcess
 * @Description: process the segment to sentences
 * @author: left
 * @date: 2014.3.10 8:17:02
 */

public class SentenceProcess {

	public static final String	SENTENCE_REG	= "(?<=\\.\\s|\\!|\\?)";

	public static Email splitSentence(Email e) {
		ArrayList<Segment> segments = e.getEmailContent().getSegments();

		for (Segment segment : segments) {
			if (segment.getContentType() == Segment.NORMAL_CONTENT) {
				ArrayList<Sentence> sentences = regSplit(segment.getSentences());
				segment.setSentences(sentences);
			}
		}

		e.getEmailContent().setSegments(segments);
		return e;
	}

	private static ArrayList<Sentence> regSplit(ArrayList<Sentence> sentences) {
		ArrayList<Sentence> result = new ArrayList<Sentence>();
		StringBuilder sb = new StringBuilder();
		for (Sentence sentence : sentences) {
			sb.append(sentence.toString() + " ");
		}
		String content = sb.toString().trim();
		String[] content2Sentence = content.split(SENTENCE_REG);

		for (String s : content2Sentence)
			result.add(new Sentence(s.trim()));

		return result;
	}

	public static void main(String args[]) {
		// String reg = "(?<=\\.\\s|\\!|\\?)";
		String reg = "[^0-9a-zA-Z\\.]";
		// ArrayList<String> lines =
		// ReadFile.readFileLines("D:/lab/final/test.txt");
		ArrayList<String> lines = new ArrayList<String>();
		String str = "A.b sss .d xx. c";
		lines.add(str);
		StringBuilder sb = new StringBuilder();
		for (String sentence : lines) {
			sb.append(sentence.toString() + " ");
		}
		String content = sb.toString().trim();
		String[] result = content.split(reg);
		for (String s : result) {
			System.out.println(s);
		}

	}
}
