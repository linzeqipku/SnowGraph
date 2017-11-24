package exps.graphlocater.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import exps.graphlocater.wrapper.CodeTermInfo;
import exps.graphlocater.wrapper.PhraseInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentParser {
    public static final Logger logger						= Logger.getLogger(DocumentParser.class);

	public static final String		PARAGRAPH_CODE_PREFIX		= "<CODE>";
	public static final String		PARAGRAPH_END_MARK			= "\n<EOP>\n";
	private static final String		SENTENCE_CODE_MASK			= "CODEMASK@";
	public static final String[]	CODE_LINE_SUFFIXES			= new String[] { ";", "{", "}" };
	private static final String		CODE_TERM_SEPARATOR			= "<CTS>";
	public static final String		PATH_THREAD_TITLE			= "Thread";
	public static final String		PATH_POST_BODY				= "Post";
	public static final String		PATH_COMMENT_TEXT			= "Comment";
	public static final String		PATH_CONTENT				= "Content";
	public static final String		PATH_PARAGRAPH				= "Paragraph";
	public static final String		PATH_SENTENCE				= "Sentence";
	public static final String		PATH_PHRASE					= "Phrase";
	public static final String		PATH_ID_MARKER				= "@";
	public static final String		PATH_SEPARATOR				= "/";

    public static boolean hasTooManyIllegalSymbols(String text) {
		int alphaCount = 0; // 特殊（code-like）符号计数
		int numberCount = 0;
		for (int i = 0; i < text.length(); i++) {
			if (Character.isAlphabetic(text.charAt(i)))
				alphaCount++;
			else if (Character.isDigit(text.charAt(i)))
				numberCount++; // 不是字母或数字
		}
		return 1.5 * alphaCount <= text.length(); // 非字母比例 >= 1/3 才返回真
	}

	/**
	 * Replace code like terms in the sentence with masks, return the terms.
	 *
	 * @param sentence
	 * @return a pair of which the left is the masked sentence and the right is
	 *         an array of the code like terms found in the sentence
	 */
	public static Pair<String, CodeTermInfo[]> maskCodeTerms(String sentence) {
		if (StringUtils.isBlank(sentence))
			return Pair.of(sentence, new CodeTermInfo[0]);

		List<CodeTermInfo> codeTerms = new ArrayList<>();

		/** find code terms by html tags. **/
		String codeTagBegin = "<code>";
		String codeTagEnd = "</code>";

		for (int codeMaskNum = 0; codeMaskNum < sentence.length(); codeMaskNum++) {
			int beginIndex = sentence.indexOf(codeTagBegin);
			if (beginIndex < 0)
				break;
			int endIndex = sentence.indexOf(codeTagEnd);

			String codeOriginal = sentence.substring(beginIndex, endIndex + codeTagEnd.length());
			String currMask = SENTENCE_CODE_MASK + codeMaskNum;

			/* Must not use replaceFirst here. Will lead to unstoppable error
			 *<code>foundTerm.text().startsWith("https://") ||
			 *foundTerm.text().startsWith("http://")</code>
			 *The code segment might contain odd chars (escape chars) to
			 *confuse the regex match
			 *text = text.replaceFirst(codeOriginal, currMask);
			 */
			sentence = StringUtils.replaceOnce(sentence, codeOriginal, currMask);

			CodeTermInfo codeTerm = new CodeTermInfo();
			codeTerm.setOriginal(codeOriginal);
			codeTerm.setMask(currMask);
			codeTerm.setIndex(codeMaskNum);

			codeTerms.add(codeTerm);

			sentence = StringUtils.replaceOnce(sentence, codeOriginal, currMask);
		}

		return Pair.of(sentence, codeTerms.toArray(new CodeTermInfo[codeTerms.size()]));
	}

    public static void unmaskCodeTerms(PhraseInfo phrase, CodeTermInfo[] codeTerms){
        if (codeTerms.length == 0){
            return;
        }
        String text = phrase.getText();
        Pattern pattern = Pattern.compile(SENTENCE_CODE_MASK + "(\\d+)");
        Matcher matcher =pattern.matcher(text);
        while(matcher.find()){
            int beginIdx = matcher.start();
            int endIdx = matcher.end();
            int numBeginIdx = matcher.start(1);
            int codeTermIdx = Integer.parseInt(text.substring(numBeginIdx, endIdx));
            text = text.substring(0, beginIdx) + codeTerms[codeTermIdx].getCodeTerm() + text.substring(endIdx);
        }
        phrase.setText(text);
    }

	public static String concatenateCodeTerms(CodeTermInfo[] codeTerms) {
		if (codeTerms == null)
			return "";
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < codeTerms.length; i++) {
			if (i > 0)
				str.append(CODE_TERM_SEPARATOR);
			str.append(codeTerms[i].getOriginal());

		}
		return str.toString();
	}
}
