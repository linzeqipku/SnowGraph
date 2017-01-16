package extractors.miners.mailqa.content;

import java.util.ArrayList;

import extractors.miners.mailqa.content.stacktrace.StackJudge;
import extractors.miners.mailqa.entity.Email;
import extractors.miners.mailqa.entity.Segment;

public class StackTraceClassifier implements CommonClassifier {

	@Override
	public void getClassificationType(Email e) {
		// TODO Auto-generated method stub
		ArrayList<Segment> segments = e.getEmailContent().getSegments();
		for (Segment segment : segments) {
			if (segment.getContentType() == Segment.NORMAL_CONTENT) {
				if (StackJudge.isStackTrace(segment)) {
					segment.setContentType(Segment.STACK_CONTENT);
				//	segment = StackJudge.shortStackTrace(segment);
				}
			}
		}
	}

}
