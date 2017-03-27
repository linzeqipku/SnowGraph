package extractors.miners.mailqa.content;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

//import extractors.miners.mailqa.data.DataSelector;
import extractors.miners.mailqa.entity.Email;
import extractors.miners.mailqa.entity.Segment;
import extractors.miners.mailqa.entity.Sentence;
//import extractors.miners.mailqa.testr.ExampleSelector;

/**
 * @ClassName: SignatureClassifier
 * @Description: TODO 识别邮件签名段落的类 方法：通过对样例邮件分析得出以下规则 规则一： 邮件签名通常是以‘--’开始的行 规则二：
 *               邮件签名通常包含电话号码，传真号码，邮箱地址 注意 邮件签名之后的段落暂时忽略 即视为邮件签名
 * @author: left
 * @date: 2014年1月2日 上午10:00:25
 */

public class SignatureClassifier implements CommonClassifier {

	private static String	SIGNATURE_LINE	= "--";
	
	private static String 	SIGNATURE_LINE2 = "__";

	public void getClassificationType2(Email e) {
		ArrayList<Segment> segments = e.getEmailContent().getSegments();
		for (int i = 0; i < segments.size(); i++) {
			Segment segment = segments.get(i);
			boolean isSig = false;

			if (segment.getContentType() == Segment.NORMAL_CONTENT) {
				ArrayList<Sentence> sentences = segment.getSentences();
				if (isSignatureStart(sentences.get(0).getSentence())) {
					isSig = true;
				}

				if ((segments.size() - i <= 2) && containsSignatureTag(segments.get(i))) {
					// System.out.println("###############################");
					// System.out.println(segments.get(i).getContentText());
					// System.out.println("###############################");
					segments.get(i).setContentType(Segment.SIGNATURE_CONTENT);
					isSig = true;
				}
			}

			if (isSig) {
				for (; i < segments.size(); i++) {
					segments.get(i).setContentType(Segment.SIGNATURE_CONTENT);
				}
			}

		}
		e.getEmailContent().setSegments(segments);
	}
	
	public void getClassificationType(Email e) {
		
		ArrayList<Segment> segments = e.getEmailContent().getSegments();
		for(int i = 0; i < segments.size(); i++) {
			Segment segment = segments.get(i);
			if(segment.getContentType() == Segment.NORMAL_CONTENT) {
				ArrayList<Sentence> sentences = segment.getSentences();
				if(isSignatureStart(sentences.get(0).getSentence())) {
					segments.get(i).setContentType(Segment.SIGNATURE_CONTENT);
					for(; i < segments.size(); i++) {
						segments.get(i).setContentType(Segment.SIGNATURE_CONTENT);
					}
				}
				else if ((segments.size() - i <= 2) && containsSignatureTag(segments.get(i))) {
					segments.get(i).setContentType(Segment.SIGNATURE_CONTENT);
					for(; i < segments.size(); i++) {
						segments.get(i).setContentType(Segment.SIGNATURE_CONTENT);
					}
				}
			}
		}
		
		e.getEmailContent().setSegments(segments);
	}

	private static boolean isSignatureStart(String str) {
		if (str == null || str.length() == 0)
			return false;
		if (str.trim().toLowerCase().startsWith(SIGNATURE_LINE) || str.trim().startsWith(SIGNATURE_LINE2))
			return true;
		return false;
	}

	private static boolean containsSignatureTag(Segment segment) {
		ArrayList<Sentence> sentences = segment.getSentences();
		for (Sentence sentence : sentences) {
			if (sentence.toString().trim().startsWith(SIGNATURE_LINE) || sentence.toString().startsWith(SIGNATURE_LINE2)) {
				return true;
			}
		}
		return false;
	}

	public static void main(String args[]) {
		Segment seg = new Segment();
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();
		String sentence = "---sdasd";
		String pre = "sdasd";
		sentences.add(new Sentence(pre));
		sentences.add(new Sentence(sentence));
		seg.setSentences(sentences);
		System.out.println(containsSignatureTag(seg));

	}
}
