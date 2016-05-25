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

public class QaCommentExtractor
{

	Set<Integer> questionIdSet = new HashSet<Integer>();
	Set<Integer> answerIdSet = new HashSet<Integer>();
	Set<Integer> userIdSet = new HashSet<Integer>();
	
	static Pattern postIdRe = Pattern.compile("postid=\"(\\d+)\"");
	static Pattern userIdRe = Pattern.compile("userid=\"(\\d+)\"");
	
	public QaCommentExtractor(Set<Integer> questionIdSet, Set<Integer> answerIdSet)
	{
		this.questionIdSet = questionIdSet;
		this.answerIdSet = answerIdSet;
	}

	public void extractQaCommentXmlFile(String commentXmlPath, String dstXmlPath)
	{

		BufferedReader br = null;
		FileWriter bw = null;
		try
		{
			br = new BufferedReader(new BufferedReader(new InputStreamReader(new SmbFileInputStream(new SmbFile(commentXmlPath)))));
			bw = new FileWriter(dstXmlPath);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String str = null;
		int i=0;
		try
		{
			bw.write("<comments>\r\n");
			while ((str = br.readLine()) != null)
			{
				i++;
				Matcher matcher=postIdRe.matcher(str.toLowerCase());
				if (!matcher.find())
					continue;
				int postId = Integer.parseInt(matcher.group(1));
				if (questionIdSet.contains(postId)||answerIdSet.contains(postId)){
					System.out.println("第"+i+"行: 对帖子"+postId+"的评论；");
					bw.write(str + "\r\n");
					
					//提取Comment User
					matcher = userIdRe.matcher(str.toLowerCase());
					if(!matcher.find()){
						continue;
					}
					int userId = Integer.parseInt(matcher.group(1));
					userIdSet.add(userId);
					System.out.println("第"+i+"行: 对帖子"+postId+"的评论的用户为" + userId + ";");
				}
			}
			bw.write("</comments>");
			br.close();
			bw.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		String projectName = ".net";
		QuestionExtractor extractor = new QuestionExtractor(projectName);
		extractor.extractQuestionXmlFile("C:/Users/shutear/Downloads/es.stackoverflow.com/posts.xml", "d:/question_out.xml");
		
		System.out.println("***************************************");
		
		AnswerExtractor answerExtractor = new AnswerExtractor(extractor.questionIdSet);
		answerExtractor.extractAnswerXmlFile("C:/Users/shutear/Downloads/es.stackoverflow.com/posts.xml", "d:/answer_out.xml");

		System.out.println("***************************************");
		QaCommentExtractor qaCommentExtractor = new QaCommentExtractor(extractor.questionIdSet, answerExtractor.answerIdSet);
		qaCommentExtractor.extractQaCommentXmlFile("C:/Users/shutear/Downloads/es.stackoverflow.com/Comments.xml", "d:/qa_comment_out.xml");
		System.out.println("项目" + projectName + " 评论的用户id如下：");
		for(int userId: qaCommentExtractor.userIdSet){
			System.out.print(userId + "\t");
		}
	}
}
