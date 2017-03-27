package extractors.miners.codesnippet.mail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class MailBodyProcessor {

	private static final String SIGNATURE_LINE = "--";
	private static final String SIGNATURE_LINE2 = "__";

	public static final String	REF_LINE	= "wrote:";
	public static final String	REF_TAG		= ">";

	private static boolean isSignatureStart(String str) {
		return !(str == null || str.length() == 0) && (str.trim().toLowerCase().startsWith(SIGNATURE_LINE) || str.trim().startsWith(SIGNATURE_LINE2));
	}

	private static boolean containsSignatureTag(Segment segment) {
		List<String> sentences = segment.getSentences();
		for (String sentence : sentences) {
			if (sentence.trim().startsWith(SIGNATURE_LINE) || sentence.startsWith(SIGNATURE_LINE2)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isReference(Segment segment) {
		boolean isRef = false;
		int totalLine;
		int arrowLine = 0;

		List<String> sentences = segment.getSentences();
		totalLine = sentences.size();

		for (int i = 0; i < totalLine; i++) {
			String line = sentences.get(i);
			if (i == 0 && line.toLowerCase().trim().endsWith(REF_LINE)) {
				isRef = true;
				break;
			}
			if (line.toLowerCase().trim().startsWith(REF_TAG)) {
				arrowLine++;
			}
		}

		if (arrowLine >= totalLine * 0.5) {
			isRef = true;
		}
		return isRef;
	}

	public static List<String> bodyToLines(String text) {
		BufferedReader in = new BufferedReader(new StringReader(text));
		ArrayList<String> lineList = new ArrayList<>();
		String line;
		try {
			while ((line = in.readLine()) != null) {
				lineList.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lineList;
	}

	public static List<Segment> linesToSegments(List<String> lines) {
		ArrayList<Segment> segmentList = new ArrayList<>();
		Segment seg;
		ArrayList<String> sentenceList = new ArrayList<>();
		boolean start = true;
		for (String line : lines) {
			// 忽略段落开始的空行
			if (start && line.trim().isEmpty()) continue;
			// 一个段落开始
			if (start) {
				sentenceList = new ArrayList<>();
				start = false;
			}
			// 将一行话加入段落
			if (!line.trim().isEmpty()) {
				sentenceList.add(line);
				continue;
			}
			// 一个段落结束
			seg = new Segment(sentenceList);
			segmentList.add(seg);
			start = true;
		}
		return segmentList;
	}

	public static void filterSignature(List<Segment> segments) {
		List<Segment> signatures = new ArrayList<>();
		for (int i = 0; i < segments.size(); i++) {
			Segment segment = segments.get(i);
			List<String> sentences = segment.getSentences();
			if (isSignatureStart(sentences.get(0))) {
				signatures.add(segment);
				for (; i < segments.size(); i++) {
					signatures.add(segments.get(i));
				}
				break;
			} else if ((segments.size() - i <= 2) && containsSignatureTag(segments.get(i))) {
				signatures.add(segment);
				for (; i < segments.size(); i++) {
					signatures.add(segments.get(i));
				}
			}
		}
		segments.removeAll(signatures);
	}

	public static void filterReference(List<Segment> segments) {
		List<Segment> references = new ArrayList<>();
		for (Segment segment : segments) {
			if (isReference(segment)) references.add(segment);

		}
		segments.removeAll(references);
	}

	public static List<Segment> getCodes(List<Segment> segments) {
		for (Segment seg : segments) {
			if (CodeJudge.isCode(seg.getText())) {
				if (seg.getSentenceNumber() < 200) seg.setCode(true);
			}
		}
		List<Segment> mergedSegment;
		mergedSegment = CodeMerge.continualCodeMerge(segments);
		mergedSegment = CodeMerge.SplitCodeSegment(mergedSegment);
		for (Segment seg : mergedSegment) {
			if (!seg.isCode()) {
				if (CodeJudge.isCode(seg.getText())) {
					if (seg.getSentenceNumber() < 200) seg.setCode(true);
				}
			}
		}
		mergedSegment = CodeMerge.SplitCodeSegment(mergedSegment);
		return mergedSegment;
	}

}
