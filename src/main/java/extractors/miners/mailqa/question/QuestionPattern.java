package extractors.miners.mailqa.question;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import extractors.miners.mailqa.tag.TagEncoder;
import extractors.miners.mailqa.tag.Tagger;
import extractors.miners.mailqa.utils.ReadFile;
import extractors.miners.mailqa.utils.StringUtils;

/**
 * @ClassName: QuestionPattern
 * @Description: 
 *               根据LSP学习出来的一些问题的模式，已经保存到了文件pattern.txt中，从文件中读取这些pattern，以便之后的问句标注中使用
 * @author: left
 * @date: 2014年3月24日 上午11:13:27
 */

public class QuestionPattern {

	public static ArrayList<String>	patterns		= new ArrayList<String>();

	public static ArrayList<String>	rules			= new ArrayList<String>();

//	public static final String		PATTERN_FILE	= "D:/EclipseSpace/EFAQ/lab/patterns.txt";
//
//	public static final String		RULE_FILE		= "D:/EclipseSpace/EFAQ/lab/rules.txt";
//
//	public static final String		PATTERN_FILE2	= "D:/EclipseSpace/EFAQ/lab/patterns_5W1H.txt";
	
	public static final String		PATTERN_FILE	= System.getProperty("user.dir")+"/src/main/java/extractors/miners/mailqa/patterns.txt";

	public static final String		RULE_FILE		= System.getProperty("user.dir")+"/src/main/java/extractors/miners/mailqa/rules.txt";

	public static final String		PATTERN_FILE2	= System.getProperty("user.dir")+"/src/main/java/extractors/miners/mailqa/patterns_5W1H.txt";

	public static ArrayList<String>	patterns2		= new ArrayList<String>();

	public static ArrayList<String> getPatterns() {
		if (patterns.size() == 0) {
			patterns = getPatternsFromFile();
		}
		return patterns;
	}

	public static ArrayList<String> getPatterns2() {
		if (patterns2.size() == 0) {
			patterns2 = getPatterns2FromFile();
		}
		return patterns2;
	}

	public static ArrayList<String> getPatterns2FromFile() {
		ArrayList<String> lines = ReadFile.readFileLines(PATTERN_FILE2);
		patterns2.addAll(lines);
		return patterns2;
	}

	public static ArrayList<String> getPatternsFromFile() {
		ArrayList<String> lines = ReadFile.readFileLines(PATTERN_FILE);
		patterns.addAll(lines);
		return patterns;
	}

	public static ArrayList<String> getRules() {
		if (rules.size() == 0) {
			rules = getRulesFromFile();
		}
		return rules;
	}

	public static ArrayList<String> getRulesFromFile() {
		ArrayList<String> lines = ReadFile.readFileLines(RULE_FILE);
		rules.addAll(lines);
		return rules;
	}

	public static boolean isPattern(String text) {
		if (patterns.size() == 0) {
			patterns = getPatternsFromFile();
		}

		String taggedText = TagEncoder.textToTagged(text);

		for (String pattern : patterns) {
			if (StringUtils.isCommonSequence(taggedText.toLowerCase(), pattern.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @Title:QuestionPattern
	 * @Description:5W1H类型的问题
	 * @param text
	 * @return
	 */

	public static boolean isPattern2(String text) {
		if (patterns2.size() == 0) {
			patterns = getPatterns2FromFile();
		}
		for (String pattern : patterns2) {
			if (StringUtils.isCommonSequence(text.toLowerCase(), pattern.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isRule(String rawText) {
		if (rules.size() == 0) {
			rules = getRulesFromFile();
		}

		for (String rule : rules) {
			if (StringUtils.isCommonSequence(rawText.toLowerCase(), rule.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static void main(String args[]) {
		String text = "Multisearcher will maintain index order sorting?";

		System.out.println(QuestionPattern.isPattern2(text.toLowerCase()));

//		System.out.println(PATTERN_FILE2);
//		File f = new File(PATTERN_FILE2);
//		FileInputStream fin = null;
//		try {
//			fin = new FileInputStream(f);
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		byte[] bs=new byte[1024];	
//		int count=0;		
//		try {
//			while((count=fin.read(bs))>0)				
//			{
//				String str=new String(bs,0,count);	//反复定义新变量：每一次都 重新定义新变量，接收新读取的数据
//				System.out.println(str);		//反复输出新变量：每一次都 输出重新定义的新变量
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			fin.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
