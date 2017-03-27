package extractors.miners.mailqa.content;

import java.util.ArrayList;

import extractors.miners.mailqa.entity.Email;
import extractors.miners.mailqa.entity.Segment;
import extractors.miners.mailqa.entity.Sentence;

/**
 * @ClassName: JunkClassifier
 * @Description: TODO judge the junk content
 * @author: left
 * @date: 2014.3.7 10:16:43
 */

public class JunkClassifier implements CommonClassifier {

	public static String	greetings[]		= { "hi", "hello" };

	public static final int	SHORT_SEGMENT	= 5;

	@Override
	public void getClassificationType(Email e) {
		// TODO Auto-generated method stub
		ArrayList<Segment> segments = e.getEmailContent().getSegments();

		for (int i = 0; i < segments.size(); i++) {
			Segment segment = segments.get(i);
			if (segment.getContentType() == Segment.NORMAL_CONTENT) {
				if (isGreeting(segment, i) || isShortSegment(segment)) {
					segment.setContentType(Segment.JUNK_CONTENT);
				}
			}
		}
		e.getEmailContent().setSegments(segments);

	}

	public static boolean isGreeting(Segment segment, int index) {
		boolean result = false;
		ArrayList<Sentence> sentences = segment.getSentences();

		if (sentences.size() <= 2 && index <= 1) {
			for (Sentence sentence : sentences) {
				String line = sentence.toString().trim().toLowerCase();
				for (int i = 0; i < greetings.length; i++) {
					if (line.contains(greetings[i])) {
						result = true;
					}
				}
			}
		}

		return result;
	}

	public static boolean isShortSegment(Segment segment) {
		boolean result = false;

		ArrayList<Sentence> sentences = segment.getSentences();
		if (sentences.size() == 1) {
			String line = sentences.get(0).toString();
			String words[] = line.split(" ");
			if (words.length <= SHORT_SEGMENT)
				result = true;

		}

		return result;
	}

}
