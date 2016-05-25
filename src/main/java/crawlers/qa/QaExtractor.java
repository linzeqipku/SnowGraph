package crawlers.qa;

import java.util.HashSet;
import java.util.Set;

import utils.Config;

public class QaExtractor
{

	static String remoteQaPath=Config.getValue("stackoverflowdump", "");
	
	public static void extract(String projectName, String qPath, String aPath, String cPath, String uPath, String plPath){
		QuestionExtractor qExtractor=new QuestionExtractor(projectName);
		String postXmlPath=remoteQaPath+"/Posts.xml";
		qExtractor.extractQuestionXmlFile(postXmlPath, qPath);
		AnswerExtractor aExtractor=new AnswerExtractor(qExtractor.questionIdSet);
		aExtractor.extractAnswerXmlFile(postXmlPath, aPath);
		QaCommentExtractor cExtractor=new QaCommentExtractor(qExtractor.questionIdSet, aExtractor.answerIdSet);
		String commentXmlPath=remoteQaPath+"/Comments.xml";
		cExtractor.extractQaCommentXmlFile(commentXmlPath, cPath);
		
		//汇总Question, Answer 和 Comment对应用户的Id
		Set<Integer> allUserIdSet = new HashSet<Integer>();
		allUserIdSet.addAll(qExtractor.userIdSet);
		allUserIdSet.addAll(aExtractor.userIdSet);
		allUserIdSet.addAll(cExtractor.userIdSet);
		//提取Question, Answer 和 Comment对应用户的详细信息
		QaUserExtractor uExtractor=new QaUserExtractor(allUserIdSet);
		String userXmlPath=remoteQaPath+"/Users.xml";
		uExtractor.extractQuestionXmlFile(userXmlPath, uPath);
		
		//提取Question之间存在的Duplicate关系
		PostLinkExtractor plExtractor = new PostLinkExtractor(qExtractor.questionIdSet);
		String postLinksXmlPath=remoteQaPath+"/PostLinks.xml";
		plExtractor.extractQuestionXmlFile(postLinksXmlPath, plPath);
	}
}
