package extractors.miners.mailqa.question;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import extractors.miners.mailqa.content.MessageProcess;
import extractors.miners.mailqa.content.SentenceProcess;
//import extractors.miners.mailqa.dao.MessageDao;
//import extractors.miners.mailqa.dao.QADao;
//import extractors.miners.mailqa.dao.SessionDao;
//import extractors.miners.mailqa.dao.TagDao;
import extractors.miners.mailqa.entity.Email;
import extractors.miners.mailqa.entity.QuestionAnswer;
import extractors.miners.mailqa.entity.Segment;
import extractors.miners.mailqa.entity.Sentence;
import extractors.miners.mailqa.entity.Session;
import extractors.miners.mailqa.entity.Tag;
import extractors.miners.mailqa.tag.Keywords;
import extractors.miners.mailqa.utils.VariableNameUtils;

class Candidate implements Comparable<Candidate> {

	String	sentence	= "";
	int		weight		= 0;

	Candidate() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object) 按照权重从大道小排序
	 */
	@Override
	public int compareTo(Candidate c) {
		return c.weight - this.weight;

	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("sentence: " + sentence + "\n");
		sb.append("weight: " + weight + "\n");
		return sb.toString();

	}
}

/**
 * @ClassName: QuestionClassifier
 * @Description: 标注出邮件中的问题句式 现阶段 根据几个固定的模式 以及5W1H来对问题进行标注 问题句暂时保存到数据库中
 *               展示时匹配文本并高亮显示
 * @author: left
 * @date: 2014年3月24日 上午9:56:30
 */

public class QuestionClassifier {

	public static final String[]	QUESTION_WORDS	= { "what", "What", "why", "Why", "which",
			"Which", "when", "When", "where", "Where", "how", "How", "?" };

	public static final String[]	QUESTION_WORDS2	= { "is", "Is", "Are", "are", "can", "Can",
			"Could", "could"						};

	public static final String[]	QUESTION_WORDS3	= { "can", "Can", "Could", "could", "wonder",
			"wondering"							};

	public static boolean isNormalQuestion(String text) {
		for (String word : QUESTION_WORDS) {
			if (text.startsWith(word))
				return true;
		}

		if (text.endsWith("?")) {
			for (String word : QUESTION_WORDS2) {
				if (text.startsWith(word))
					return true;
			}
		}

		return false;
	}

	public static boolean isRuleQuestion(String text) {
		return QuestionPattern.isRule(text);
	}

	public static boolean isPatternQuestion(String text) {
		return QuestionPattern.isPattern2(text);
	}

	/**
	 * @Title:QuestionClassifier
	 * @Description:对一个已经处理过的邮件，抽取其中的问题句子 策略如下，
	 *                                    1、首先判断邮件标题是不是问句，若是问句则返回该问句作为当前邮件的问句
	 *                                    2、若标题不是问句，则先抽取出标题中的内容词(非停用词及功能词)
	 *                                    3、对NORMAL_SEGMENT中的自然语言文本逐句判断是否是问句
	 *                                    ，若是问句加入候选答案
	 *                                    4、对候选答案进行打分，选择最佳的一个问句(暂时简单做)
	 * @param e
	 * @return
	 */

	public String getQuestionSentence(Email e) {
		String question = "";
		if (isQuestion(e.getSubject())) {
			System.out.println(e.getSubject() + " length is :" + e.getSubject().split(" ").length);
			question = e.getSubject();
			return question;
		}

		HashSet<String> contentWords = getContentWords(e.getSubject());
		ArrayList<Candidate> candidates = getCandidates(e, contentWords);
		System.out.println(candidates.size());
		Collections.sort(candidates);
		if (candidates != null && candidates.size() > 0)
			question = candidates.get(0).sentence;
		else
			question = e.getSubject();
		return question;
	}

	/**
	 * @Title:QuestionClassifier
	 * @Description:获取问题的同时对问题进行打分 打分机制 对于问号结尾的句子 得1分 对于rule或者pattern的question
	 *                             得两分 对于包含标题中内容词的句子 每包含一个词 得1分 对于包含包含代码元素的
	 *                             每包含一个词的1分
	 * @param e
	 * @return
	 */

	public ArrayList<Candidate> getCandidates(Email e, HashSet<String> contentWords) {
		ArrayList<Segment> segments = e.getEmailContent().getSegments();
		ArrayList<Candidate> result = new ArrayList<Candidate>();
		for (Segment segment : segments) {
			if (segment.getContentType() == Segment.NORMAL_CONTENT) {
				ArrayList<Sentence> sentences = segment.getSentences();
				for (Sentence sentence : sentences) {
					String text = sentence.getSentence();
					System.out.println(text);
					if (isQuestion(text)) {
						Candidate tempCandidate = new Candidate();
						tempCandidate.sentence = sentence.getSentence();
						tempCandidate.weight += 2;
						HashSet<String> words = getContentWords(text);
						// 问号结尾 加一分
						if (text.endsWith("?")) {
							tempCandidate.weight += 1;
						}
						// 每包含标题词一个 加一分
						for (String word : words) {
							if (contentWords.contains(word)) {
								tempCandidate.weight += 1;
							}
						}
						// 每包含代码元素一个 加1分
						tempCandidate.weight += VariableNameUtils.countCamelCase(text);

						result.add(tempCandidate);
					}
				}
			}
		}
		return result;
	}

	/**
	 * @Title:QuestionClassifier
	 * @Description:判断一句话是不是问句
	 * @param text
	 * @return
	 */
	public static boolean isQuestion(String text) {
		if (text.split("\\s+").length <= 5)
			return false;
		if (isRuleQuestion(text) || isPatternQuestion(text))
			return true;
		return false;
	}

	public static HashSet<String> getContentWords(String text) {
		HashSet<String> result = new HashSet<String>();
		String[] words = text.split("\\s+");
		Keywords ky = new Keywords();
		for (String word : words) {
			if (isWord(word)) {
				if (!ky.isFunctionWords(word) && !ky.isStopWords(word)) {
					result.add(word.toLowerCase());
				}
			}
		}
		return result;
	}

	private static boolean isWord(String word) {
		if (word == null || word.length() == 0)
			return false;
		word = word.toLowerCase();
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			if (c >= '0' && c <= '9')
				continue;
			if (c >= 'a' && c <= 'z')
				continue;

			return false;
		}
		return true;
	}

	public void getQuestionOfAllSamples() {
		/*TagDao td = new TagDao();
		SessionDao sd = new SessionDao();

		MessageProcess messageProcess = new MessageProcess();
		QuestionClassifier questionClassifier = new QuestionClassifier();
		// QADao qaDao = new QADao();
		// PrintStream ps = null;
		// try {
		// ps = new PrintStream(new FileOutputStream(new
		// File("D:/lab/final/test.txt")));
		// }
		// catch (FileNotFoundException e1) {
		// e1.printStackTrace();
		// }

		ArrayList<Tag> tagList = td.getAllTags();

		int count = 0;
		for (Tag tag : tagList) {
			// if(tag.getRaw_text() != null && tag.getRaw_text().length() > 0 &&
			// (tag.getTagged_text() == null || tag.getTagged_text().length() ==
			// 0)) {
			if (tag.getRaw_text() != null && tag.getRaw_text().length() > 0) {
				String sessionUuid = tag.getSession_uuid();
				Session session = sd.getSessionByUuid(sessionUuid);
				Email e = Session.getPromoter(session);
				if (e != null) {
					messageProcess.process(e);
					QuestionAnswer qa = new QuestionAnswer();
					qa.setSession_uuid(session.getSessionID());
					qa.setMessage_uuid(e.getMessageID());
					String question = questionClassifier.getQuestionSentence(e);
					tag.setTagged_text(question);
					System.out.println(tag.session_uuid);
					System.out.println(question);
					td.updateTag(tag);
					count++;
					// if(count >= 10 ) break;
				}
			}
		}*/
	}

	// 测试一下
	public void getQuestionsOfAllSessions() {
		/*SessionDao sd = new SessionDao();
		ArrayList<Session> sessions = sd.getAllSession();
		ArrayList<Session> temp = new ArrayList<Session>();
		for (int i = 0; i < 100; i++)
			temp.add(sessions.get(i));

		MessageProcess messageProcess = new MessageProcess();
		QuestionClassifier questionClassifier = new QuestionClassifier();
		QADao qaDao = new QADao();
		PrintStream ps = null;
		try {
			ps = new PrintStream(new FileOutputStream(new File("D:/lab/final/test.txt")));
		}
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		for (Session session : temp) {
			Email e = Session.getPromoter(session);
			if (e != null) {
				messageProcess.process(e);
				QuestionAnswer qa = new QuestionAnswer();
				qa.setSession_uuid(session.getSessionID());
				qa.setMessage_uuid(e.getMessageID());
				String question = questionClassifier.getQuestionSentence(e);

				System.setOut(ps);
				System.out.println("#################################3");
				System.out.println(e.getSubject());
				System.out.println(question);
				System.out.println(e.getContent());
				System.out.println("#################################3");
				qa.setQuestion(question);

				qaDao.insertQA(qa);
			}
		}*/

	}

	public static void main(String args[]) {
		// new QuestionClassifier().getQuestionsOfAllSessions();
		new QuestionClassifier().getQuestionOfAllSamples();
		// MessageDao md = new MessageDao();
		// Email e =
		// md.getEmailByMessageId("<f514763f0701290150x24fa23f0t923a62f8088ecbfb@mail.gmail.com>");
		// QuestionClassifier qc = new QuestionClassifier();
		// MessageProcess messageProcess = new MessageProcess();
		// messageProcess.process(e);
		// System.out.println(qc.getQuestionSentence(e));

	}
}
