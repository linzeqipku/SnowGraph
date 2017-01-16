package extractors.miners.mailqa.tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @ClassName: TagEncoder
 * @Description: TODO 对tag进行编码 方便构成向量 输入 ： 一个经过tag标注的句子 这其中的非关键词都已经被标注过
 * @author: left
 * @date: 2014年3月12日 上午11:23:33
 */

public class TagEncoder {

	public static HashMap<Integer, String>	tagMap	= new HashMap<Integer, String>();

	public static HashMap<String, Integer>	codeMap	= new HashMap<String, Integer>();

	public static String textToTagged(String text) {
		ArrayList<Integer> intVector = new Tagger().getVector(text);
		StringBuilder sb = new StringBuilder();
		for (Integer i : intVector) {
			sb.append(tagMap.get(i) + " ");
		}
		return sb.toString();
	}

	/**
	 * @Title:TagEncoder
	 * @Description: 对标注过的语句进行编码，使其转换成可以有数字表述的序列 编码 index从0开始 1：OtherKeywords
	 *               5W1H + ? 逐个叠加 2：functionwords 逐个叠加 3: tag 逐个叠加
	 *               4：projectwords + titlewords 编码为同一个
	 */

	public static void encode() {
		int index = 0;
		Keywords ky = new Keywords();
		HashSet<String> otherKeywords = ky.getOtherKeyWordsSet();
		for (String word : otherKeywords) {
			tagMap.put(index, word);
			codeMap.put(word, index);
			index++;
		}

		HashSet<String> functionWords = ky.getFunctionWordsSet();
		for (String word : functionWords) {
			if (ky.isOtherKeywords(word))
				continue;// 跳过已经编码的词汇
			tagMap.put(index, word);
			codeMap.put(word, index);
			index++;
		}

		HashSet<String> tagWords = TagWords.getTagWords();
		for (String word : tagWords) {
			if (ky.isOtherKeywords(word) || ky.isFunctionWords(word))
				continue; // 跳过已经编码的词汇
			tagMap.put(index, word);
			codeMap.put(word, index);
			index++;
		}

		HashSet<String> projectWords = ky.getProjectWordsSet();
		for (String word : projectWords) {
			if (ky.isOtherKeywords(word) || ky.isFunctionWords(word))
				continue; // 跳过已经编码的词汇
			tagMap.put(Integer.MAX_VALUE, "PROJECT_WORD");
			codeMap.put(word, Integer.MAX_VALUE);
		}
	}

	public static HashMap<String, Integer> getCodeMap() {
		if (codeMap.size() == 0) {
			encode();
		}
		return codeMap;
	}

	public static HashMap<Integer, String> getTagMap() {
		if (tagMap.size() == 0) {
			encode();
		}
		return tagMap;
	}

	public static void main(String args[]) {
		String text = "How to get the \"context\" of the searched word ?";
		System.out.println(textToTagged(text));
	}
}
