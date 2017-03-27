package extractors.miners.mailqa.content;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

//import extractors.miners.mailqa.dao.MessageDao;
import extractors.miners.mailqa.entity.Content;
import extractors.miners.mailqa.entity.Email;
import extractors.miners.mailqa.entity.Segment;
import extractors.miners.mailqa.entity.Sentence;
import extractors.miners.mailqa.utils.ReadFile;

/**
 * @ClassName: SegmentSpliter
 * @Description: TODO split the mail to the segment by " "
 * @author: left
 * @date: 2013.12.26 10:24:33
 */

public class SegmentSpliter implements ContentProcess {

	ArrayList<String>	lineList	= new ArrayList<String>();
	ArrayList<Segment>	segmentList	= new ArrayList<Segment>();

	@Override
	public void process(Email e) {
		// 第一步：将邮件文本按行划分 读入到lineList
		lineList = readLines(e.getContent());
		// for(String line : lineList) {
		// if(line.trim().length() == 0) {
		// System.out.println("empty line");
		// }
		// else System.out.println(line.length() + " : " + line);
		// }
		// 第二步: 以空行将行分为段
		segmentList = readSegments(lineList);
		// for (Segment s : segmentList) {
		// System.out.println(s.toString());
		// }
		Content content = new Content();
		content.setSegments(segmentList);
		e.setEmailContent(content);

	}

	private ArrayList<Segment> readSegments(ArrayList<String> lineList) {
		ArrayList<Segment> segmentList = new ArrayList<Segment>();
		Segment seg = new Segment();
		ArrayList<Sentence> sentenceList = new ArrayList<Sentence>();
		boolean start = true;
		for (String line : lineList) {
			// 忽略段落开始的空行
			if (start && line.trim().isEmpty())
				continue;
			// 一个段落开始
			if (start) {
				sentenceList = new ArrayList<Sentence>();
				start = false;
			}
			// 将一行话加入段落
			if (!start && !line.trim().isEmpty()) {
				sentenceList.add(new Sentence(line));
				continue;
			}
			// 一个段落结束
			if (!start && line.trim().isEmpty()) {
				seg = new Segment();
				seg.setSentences(sentenceList);
				segmentList.add(seg);
				start = true;
			}

		}
		return segmentList;
	}

	private ArrayList<String> readLines(String content) {
		ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
		BufferedReader in = new BufferedReader(new InputStreamReader(bais));
		ArrayList<String> lineList = new ArrayList<String>();
		String line = "";
		try {
			while ((line = in.readLine()) != null) {
				lineList.add(line);
			}
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		return lineList;

	}

	public static void main(String args[]) {
//		Email e = new MessageDao().getEmailById(33);
//		System.out.println(e.getContent());
//		SegmentSpliter ss = new SegmentSpliter();
//		ss.process(e);、
		String path = "D:/test.txt";
		// File testFile = new File(path);
		String content = ReadFile.read_file_string(path);
//		System.out.println(content);
		SegmentSpliter ss = new SegmentSpliter();
		ArrayList<String> lineList = ss.readLines(content);
		ArrayList<Segment> segmentList = ss.readSegments(lineList);
		int count=0;
		for( Segment seg:segmentList)
		{
			System.out.println( "seg: "+count++ +" "+seg.getContentText());
		}
	}
}
