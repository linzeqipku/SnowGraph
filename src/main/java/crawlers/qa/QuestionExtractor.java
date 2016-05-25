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

public class QuestionExtractor
{

	Set<Integer> questionIdSet = new HashSet<Integer>();
	Set<Integer> userIdSet = new HashSet<Integer>();
	
	Pattern tagsRe=null;
	static Pattern idRe=Pattern.compile("id=\"(\\d+)\"");
	static Pattern userIdRe = Pattern.compile("owneruserid=\"(\\d+)\"");
	String projectName = "";

	public QuestionExtractor(String projectName)
	{
		this.projectName = projectName.toLowerCase();
		tagsRe=Pattern.compile("tags=\"[^\"]*"+projectName);
	}

	public void extractQuestionXmlFile(String postXmlPath, String dstXmlPath)
	{

		BufferedReader br = null;
		FileWriter bw = null;
		try
		{
			System.out.println(postXmlPath);
			br = new BufferedReader(new BufferedReader(new InputStreamReader(new SmbFileInputStream(new SmbFile(postXmlPath)))));
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
			bw.write("<questions>\r\n");
			while ((str = br.readLine()) != null)
			{
				i++;
				boolean flag=tagsRe.matcher(str.toLowerCase()).find()&&str.contains("PostTypeId=\"1\"");
				if (flag){
					bw.write(str+"\r\n");
					Matcher matcher=idRe.matcher(str.toLowerCase());
					if (!matcher.find())
						continue;
					int id=Integer.parseInt(matcher.group(1));
					questionIdSet.add(id);
					System.out.println("第"+i+"行: 问题"+id+"；");
					
					matcher = userIdRe.matcher(str.toLowerCase());
					if(!matcher.find()){
						continue;
					}
					int userId = Integer.parseInt(matcher.group(1));
					userIdSet.add(userId);
					System.out.println("第"+i+"行: User "+userId+"；");
				}
			}
			bw.write("</questions>");
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
		String projectName = "c#";
		QuestionExtractor extractor = new QuestionExtractor(projectName);
		extractor.extractQuestionXmlFile("C:/Users/shutear/Downloads/es.stackoverflow.com/posts.xml", "d:/question_out.xml");
		
		System.out.println("项目" + projectName + " 提问问题的用户的id如下：");
		for(int userId: extractor.userIdSet){
			System.out.print(userId+"\t");
		}
	}

}
