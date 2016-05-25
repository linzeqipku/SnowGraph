package crawlers.qa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class QaUserExtractor {
	private static final Pattern userIdRe = Pattern.compile("id=\"(\\d+)\"");

	private Set<Integer> userIdSet = null;

	public QaUserExtractor(Set<Integer> userIdSet) {
		this.userIdSet = userIdSet;
	}

	public void extractQuestionXmlFile(String postXmlPath, String dstXmlPath) {
		BufferedReader br = null;
		FileWriter bw = null;
		try {
			br = new BufferedReader(new BufferedReader(new InputStreamReader(new SmbFileInputStream(new SmbFile(postXmlPath)))));
			bw = new FileWriter(dstXmlPath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String str = null;
		int i = 0;
		try {
			bw.write("<users>\r\n");
			while ((str = br.readLine()) != null) {
				Matcher matcher = userIdRe.matcher(str.toLowerCase());
				if (!matcher.find()) {
					continue;
				}

				int userId = Integer.parseInt(matcher.group(1));
				if (userIdSet.contains(userId)) {
					bw.write(str + "\r\n");
					System.out.printf("第%d行: 用户%d;\n", i, userId);
				}
				i++;
			}
			bw.write("</users>");
			br.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String projectName = "c#";
		QuestionExtractor extractor = new QuestionExtractor(projectName);
		extractor.extractQuestionXmlFile("C:/Users/shutear/Downloads/es.stackoverflow.com/posts.xml", "d:/question_out.xml");
		
		System.out.println("***************************************");
		AnswerExtractor answerExtractor = new AnswerExtractor(extractor.questionIdSet);
		answerExtractor.extractAnswerXmlFile("C:/Users/shutear/Downloads/es.stackoverflow.com/posts.xml", "d:/answer_out.xml");

		System.out.println("***************************************");
		QaCommentExtractor qaCommentExtractor = new QaCommentExtractor(extractor.questionIdSet, answerExtractor.answerIdSet);
		qaCommentExtractor.extractQaCommentXmlFile("C:/Users/shutear/Downloads/es.stackoverflow.com/Comments.xml", "d:/qa_comment_out.xml");
		
		Set<Integer> allUserIdSet = new HashSet<Integer>();
		allUserIdSet.addAll(extractor.userIdSet);
		System.out.println(allUserIdSet.size());
		allUserIdSet.addAll(answerExtractor.userIdSet);
		System.out.println(allUserIdSet.size());
		allUserIdSet.addAll(qaCommentExtractor.userIdSet);
		System.out.println(allUserIdSet.size());
		
		QaUserExtractor qaUserExtractor = new QaUserExtractor(allUserIdSet);
		qaUserExtractor.extractQuestionXmlFile("C:/Users/shutear/Downloads/es.stackoverflow.com/Users.xml", "d:/qa_user_out.xml");
	}
}
