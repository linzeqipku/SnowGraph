package extractors.miners.mailqa.content;

import java.util.ArrayList;

import extractors.miners.mailqa.entity.Email;
import extractors.miners.mailqa.entity.Segment;
import extractors.miners.mailqa.entity.Sentence;

/**
 * @ClassName: ReferenceClassifier
 * @Description: 对邮件中的引用段落进行分类 引用段落有一下特征： 1、在段落中以多行以 ‘ > ’开头 2、在段落开是通常是 somebody
 *               wrote: 或者 是on sometime XX wrote:
 * @author: left
 * @date: 2014年3月6日 下午2:21:42
 */

public class ReferenceClassifier implements CommonClassifier {

	public static String	REF_LINE	= "wrote:";
	public static String	REF_TAG		= ">";

	@Override
	public void getClassificationType(Email e) {
		// TODO Auto-generated method stub
		ArrayList<Segment> segments = e.getEmailContent().getSegments();
		for (Segment segment : segments) {
			if (segment.getContentType() == Segment.NORMAL_CONTENT) {
				if (isReference(segment)) {
					segment.setContentType(Segment.REF_CONTENT);
				}
			}
		}
		e.getEmailContent().setSegments(segments);
	}

	private static boolean isReference(Segment segment) {
		boolean isRef = false;
		int totalLine = 0;
		int arrowLine = 0;

		ArrayList<Sentence> sentences = segment.getSentences();
		totalLine = sentences.size();

		for (int i = 0; i < totalLine; i++) {
			String line = sentences.get(i).toString();
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

	public static void main(String args[]) {

	}

}
