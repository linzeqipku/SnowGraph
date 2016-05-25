package crawlers.qa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

/*
 * 从PostLinks中提取Stack overflow帖子间的Duplicate关系，即 LinkTypeId = 3.
 * 
 * 参见： http://meta.stackexchange.com/questions/226837/what-is-the-meaning-of-postlinks-table
 *     http://meta.stackexchange.com/questions/2677/database-schema-documentation-for-the-public-data-dump-and-sede
 */
public class PostLinkExtractor {
	private static final Pattern postIdRe = Pattern.compile("postid=\"(\\d+)\"");
	private static final Pattern relatedPostIdRe = Pattern.compile("relatedpostid=\"(\\d+)\"");
	
	private Set<Integer> questionIdSet = null;
	
	public PostLinkExtractor(Set<Integer> questionIdSet){
		this.questionIdSet = questionIdSet;
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
			bw.write("<PostLinks>\r\n");
			while ((str = br.readLine()) != null) {
				//一条Duplicate记录
				if(str.contains("LinkTypeId=\"3\"")){
					Matcher matcher = postIdRe.matcher(str.toLowerCase());
					if(!matcher.find()){
						continue;
					}					
					int postId = Integer.parseInt(matcher.group(1));
					if(!questionIdSet.contains(postId)){
						continue;
					}
					
					matcher = relatedPostIdRe.matcher(str.toLowerCase());
					if(!matcher.find()){
						continue;
					}
					int relatedPostId = Integer.parseInt(matcher.group(1));
					if(!questionIdSet.contains(relatedPostId)){
						continue;
					}
					
					bw.write(str + "\r\n");
					System.out.printf("第%d行: Duplicate 关系: postId=%d --> relatedPostId=%d;\n", i, postId,relatedPostId);
				}
				i++;
			}
			bw.write("</PostLinks>");
			br.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String projectName = "css3";
		QuestionExtractor extractor = new QuestionExtractor(projectName);
		extractor.extractQuestionXmlFile("C:/Users/shutear/Downloads/es.stackoverflow.com/posts.xml", "d:/question_out.xml");
		
		PostLinkExtractor postLinkExtractor = new PostLinkExtractor(extractor.questionIdSet);
		postLinkExtractor.extractQuestionXmlFile("C:/Users/shutear/Downloads/es.stackoverflow.com/postlinks.xml", "d:/postlink_out.xml");
	}
}
