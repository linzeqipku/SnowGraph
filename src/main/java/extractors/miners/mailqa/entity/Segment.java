package extractors.miners.mailqa.entity;

import java.util.ArrayList;

/**
 * @ClassName: Segment
 * @Description: 段是邮件内容的基本分割单位 将邮件内容标注为以下几类 NORMAL_CONTENT 正文内容(有效的正文内容）
 *               CODE_CONTENT 代码段落 STACK_CONTENT 异常文本段落 SIGNATURE_CONTENT 邮件签名段落
 *               JUNK_CONTENT 冗余文本段落（邮件签名，问候语，邮件末尾冗余引用) REF_CONTENT
 *               引用文本段落（引用自其它邮件的文本段落，以>开头或者以:开头的行）
 * @author: left
 * @date: 2013年12月26日 上午10:31:09
 */

public class Segment {

	public static int			NORMAL_CONTENT		= 0;
	public static int			CODE_CONTENT		= 1;
	public static int			STACK_CONTENT		= 2;
	public static int			SIGNATURE_CONTENT	= 3;
	public static int			JUNK_CONTENT		= 4;
	public static int			REF_CONTENT			= 5;

	private ArrayList<Sentence>	sentences;
	private int					contentType			= 0;

	public int getContentType() {
		return contentType;
	}

	public void setContentType(int contentType) {
		this.contentType = contentType;
	}

	public ArrayList<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(ArrayList<Sentence> sentences) {
		this.sentences = sentences;
	}

	public String getContentText() {
		StringBuilder sb = new StringBuilder();
		sentences = this.getSentences();
		for (Sentence sentence : sentences) {
			sb.append(sentence.toString() + "\n");
		}
		return sb.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("segment type : " + getContentType() + "\n");
		for (Sentence s : sentences) {
			sb.append(s.toString() + "\n");
		}
		return sb.toString();
	}
}
