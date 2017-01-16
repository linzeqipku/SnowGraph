package extractors.miners.mailqa.content;

import extractors.miners.mailqa.entity.Email;

public class MessageProcess implements ContentProcess {

	public static SegmentSpliter	segmentSpliter		= new SegmentSpliter();
	public static SegmentClassifier	segmentClassifier	= new SegmentClassifier();

	@Override
	public void process(Email e) {
		// TODO Auto-generated method stub
		segmentSpliter.process(e);
//		System.out.println("After splite segment##################################################" + e.getEmailContent().getSegments().size());
//		System.out.println("##################################################");
//		System.out.println(e.getEmailContent());
//		System.out.println("##################################################");
//		System.out.println("##################################################");
		segmentClassifier.process(e);
//		System.out.println("After Classifier segment################################################## " + e.getEmailContent().getSegments().size());
//		System.out.println("##################################################");
//		System.out.println(e.getEmailContent());
//		System.out.println("##################################################");
//		System.out.println("##################################################");
		SentenceProcess.splitSentence(e);
//		System.out.println("After splite sentence segment##################################################" + e.getEmailContent().getSegments().size());
//		System.out.println("##################################################");
//		System.out.println(e.getEmailContent());
//		System.out.println("##################################################");
//		System.out.println("##################################################");
	}

}
