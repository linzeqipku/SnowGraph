package crawlers.qa;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class AnswerExtractor
{

	Set<Integer> questionIdSet = new HashSet<Integer>();
	Set<Integer> answerIdSet = new HashSet<Integer>();
	Set<Integer> userIdSet = new HashSet<Integer>();
	
	static Pattern parentIdRe=Pattern.compile("parentid=\"(\\d+)\"");
	static Pattern idRe=Pattern.compile("id=\"(\\d+)\"");
	static Pattern userIdRe = Pattern.compile("owneruserid=\"(\\d+)\"");
	
	public AnswerExtractor(Set<Integer> questionIdSet)
	{
		this.questionIdSet=questionIdSet;
	}

	public void extractAnswerXmlFile(String postXmlPath, String dstXmlPath)
	{

		BufferedReader br = null;
		FileWriter bw = null;
		try
		{
			br = new BufferedReader(new BufferedReader(new InputStreamReader(new SmbFileInputStream(new SmbFile(postXmlPath)))));
			bw = new FileWriter(dstXmlPath);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int i=0;
		String str = null;
		try
		{
			bw.write("<answers>\r\n");
			while ((str = br.readLine()) != null)
			{
				i++;
				if (str.contains("PostTypeId=\"2\"")){
					Matcher matcher=parentIdRe.matcher(str.toLowerCase());
					if (!matcher.find())
						continue;
					int parentId=Integer.parseInt(matcher.group(1));
					if (questionIdSet.contains(parentId)){
						bw.write(str+"\r\n");
						matcher=idRe.matcher(str.toLowerCase());
						if (!matcher.find())
							continue;
						int id=Integer.parseInt(matcher.group(1));
						answerIdSet.add(id);
						System.out.println("第"+i+"行: 问题"+parentId+"的答案"+id+"；");
						
						matcher = userIdRe.matcher(str.toLowerCase());
						if(!matcher.find()){
							continue;
						}
						int userId = Integer.parseInt(matcher.group(1));
						userIdSet.add(userId);
						System.out.println("第"+i+"行: 答案"+id+"的用户"+userId+"；");
					}
				}
			}
			bw.write("</answers>");
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
		
		System.out.println("项目" + projectName + " 回答问题的用户的id如下：");
		for(int userId: answerExtractor.userIdSet){
			System.out.print(userId+"\t");
		}
	}

}
