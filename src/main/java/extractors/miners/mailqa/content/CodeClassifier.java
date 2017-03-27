package extractors.miners.mailqa.content;

import java.util.ArrayList;

import extractors.miners.mailqa.content.code.CodeJudge;
import extractors.miners.mailqa.content.code.CodeMerge;
import extractors.miners.mailqa.entity.Email;
import extractors.miners.mailqa.entity.Segment;

/**
 * @ClassName: CodeClassifier
 * @Description: 识别邮件内容中的代码部分将代码从原始的segment中抽离出来 多个代码段合并成一个代码段
 *               对于每一个邮件段落Segment,若其当前标记是NORMAL_CONTENT 则Judge其是否为代码
 * @author: left
 * @date: 2014年3月5日 上午8:18:56
 */

public class CodeClassifier implements CommonClassifier {

	@Override
	public void getClassificationType(Email e) {
		ArrayList<Segment> segments = e.getEmailContent().getSegments();
		for (Segment seg : segments) {
			if (seg.getContentType() == Segment.NORMAL_CONTENT) {
				if (CodeJudge.isCode(seg.getContentText())) {
					seg.setContentType(Segment.CODE_CONTENT);
				}
			}
		}

		ArrayList<Segment> mergedSegment = CodeMerge.continualCodeMerge(segments);
		e.getEmailContent().setSegments(mergedSegment);
	}
}
