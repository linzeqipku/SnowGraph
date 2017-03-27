package extractors.miners.mailqa.content;

import java.util.ArrayList;

//import extractors.miners.mailqa.dao.MessageDao;
import extractors.miners.mailqa.entity.Email;
//import extractors.miners.mailqa.testr.ExampleSelector;

/**
 * @ClassName: SegmentClassifier
 * @Description: TODO 对segment 进行分类 NORMAL_CONTENT 正文内容(有效的正文内容） CODE_CONTENT
 *               代码段落 STACK_CONTENT 异常文本段落 SIGNATURE_CONTENT 邮件签名段落 JUNK_CONTENT
 *               冗余文本段落（邮件签名，问候语，邮件末尾冗余引用) REF_CONTENT
 *               引用文本段落（引用自其它邮件的文本段落，以>开头或者以:开头的行）
 * 
 *               对于所有是0的segment 顺序执行以下操作 1. 若该段落是NORMAL_CONTENT 识别
 *               邮件签名段落，并将邮件签名段落之后的所有段落标记为邮件签名段落 2. 若该段落是NORMAL_CONTENT
 *               识别引用文本段落，并将引用文本段落从原文本段落中分离出来 3. 若该段落是NORMAL_CONTENT
 *               识别异常文本段落，合并相邻的异常文本段落 4. 若该段落是NORMAL_CONTENT 识别代码段落,合并相邻的代码段落 5.
 *               若该段落是NORMAL_CONTENT 识别冗余文本段落 6. 剩余的段落均默认为NORMAL_CONTENT
 * @author: left
 * @date: 2013年12月30日 上午11:15:31
 */

public class SegmentClassifier implements ContentProcess {

	private CommonClassifier	commonClassifier;

	@Override
	public void process(Email e) {
		// TODO Auto-generated method stub
		// 识别邮件签名段落
		commonClassifier = new SignatureClassifier();
		commonClassifier.getClassificationType(e);

		// 识别引用文本段落，并将引用文本段落从原文本段落中分离出来
		commonClassifier = new ReferenceClassifier();
		commonClassifier.getClassificationType(e);
		// sliceReferenceSegments(e);

		// 识别异常文本段落，合并相邻的异常文本段落
		commonClassifier = new StackTraceClassifier();
		commonClassifier.getClassificationType(e);
		// mergeStackSegments(e);

		// 识别代码段落，合并相邻的代码段落
		commonClassifier = new CodeClassifier();
		commonClassifier.getClassificationType(e);
		// mergeCodeSegments(e);

		// 识别冗余文本段落
		commonClassifier = new JunkClassifier();
		commonClassifier.getClassificationType(e);

	}

	// private void sliceReferenceSegments(Email e) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// private void mergeStackSegments(Email e) {
	//
	// }
	//
	// private void mergeCodeSegments(Email e) {
	// // TODO Auto-generated method stub
	//
	// }

	/*public static void main(String args[]) {
		// test the signature classification
		ExampleSelector exampleSelector = new ExampleSelector();
		exampleSelector.select();
		ArrayList<Email> emailList = exampleSelector.getExampleEmails();
		Email email = new MessageDao().getEmailById(8);
		emailList.clear();
		emailList.add(email);
		SegmentClassifier sc = new SegmentClassifier();
		SegmentSpliter sp = new SegmentSpliter();

		for (Email e : emailList) {
			sp.process(e);
			sc.process(e);
			SentenceProcess.splitSentence(e);
			System.out.println("##########################start");
			System.out.println(e.getEmailContent());
			System.out.println("##########################end");
		}
	}*/

}