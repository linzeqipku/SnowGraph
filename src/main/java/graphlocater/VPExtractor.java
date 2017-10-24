package graphlocater;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import graphlocater.utils.DocumentParser;
import graphlocater.utils.StanfordParser;
import graphlocater.utils.TreeUtils;
import graphlocater.wrapper.CodeTermInfo;
import graphlocater.wrapper.PhraseInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;


import java.util.*;

public class VPExtractor {
    public static final Logger logger = Logger.getLogger(VPExtractor.class);
    public static final float PROOF_SCORE_THRESHOLD = -5;

    public static List<PhraseInfo> parseSentence(String sentence) {
		if (StringUtils.isBlank(sentence))
			return null;

		Pair<String, CodeTermInfo[]> textCodeMasks = DocumentParser.maskCodeTerms(sentence);
		String maskedText = textCodeMasks.getLeft();
		CodeTermInfo[] maskedCodeTerms = textCodeMasks.getRight();

		Tree tree = parseGrammaticalTree(maskedText);
		if (tree == null) // Fail to build a syntax tree.
			return null;

		PhraseInfo[] phrases = extractVerbPhrases(tree);
		int count = 0;
        for (int j = 0; j < phrases.length; j++) {
			PhraseInfo phrase = phrases[j];
			// 对每个短语进行过滤，添加proof（evidence）
			PhraseFilter.filter(phrase, maskedText);
            // 还原code term
            DocumentParser.unmaskCodeTerms(phrase, maskedCodeTerms);
            if (phrase.getProofScore() >= VPExtractor.PROOF_SCORE_THRESHOLD){
                count++;
            }
		}
        Arrays.sort(phrases, new Comparator<PhraseInfo>() {
            @Override
            public int compare(PhraseInfo o1, PhraseInfo o2) {
                return o2.getProofScore() - o1.getProofScore();
            }
        });
        if (phrases.length < 5){
            return Arrays.asList(phrases);
        }
        if (count < 5){
            return Arrays.asList(phrases).subList(0, 5);
        }
        return Arrays.asList(phrases);
	}

	public static Tree parseGrammaticalTree(String sentence) {
		if (DocumentParser.hasTooManyIllegalSymbols(sentence))
			return null;
		// Add a period to the end of sentence, if there is none.
		int i;
		for (i = sentence.length() - 1; i >= 0; i--) {
			char ch = sentence.charAt(i);
			if (Character.isLetter(ch) || Character.isDigit(ch))
				break;
		}
		sentence = sentence.substring(0, i + 1) + ".";

		// logger.info(sentence);
		Tree tree = StanfordParser.parseTreeWithoutMonitoring(sentence);
		return tree;
	}

    public static PhraseInfo[] extractVerbPhrases(Tree sentenceTree) {
		if (sentenceTree == null)
			return null;
		List<PhraseInfo> phraseList = new ArrayList<>();

		// 提取VP短语最关键的一句，定义提取的正则式
		String vpPattern = "VP < /VB.*/";
		TregexPattern tregexPattern = TregexPattern.compile(vpPattern);
		TregexMatcher matcher = tregexPattern.matcher(sentenceTree);

		HashSet<Tree> treeSet = new HashSet<>();
		// 获取下一个不同的match节点
		while (matcher.findNextMatchingNode()) {
			// match到的新的子树
			Tree matchedTree = matcher.getMatch();
			if (treeSet.contains(matchedTree)) {
				continue;
			}
			treeSet.add(matchedTree);
			// 新建一个phrase对象
			PhraseInfo phrase = new PhraseInfo();
			phrase.setPhraseType(PhraseInfo.PHRASE_TYPE_VP);
			phrase.setText(TreeUtils.interpretTreeToString(matchedTree));
			phrase.setSyntaxTree(matchedTree.toString());
			phraseList.add(phrase);
		}
		return phraseList.toArray(new PhraseInfo[phraseList.size()]);
	}

	public static void main(String[] args){
        VPExtractor extractor = new VPExtractor();
        String sentence = "Anyone who has had experience with the search engines above, or other engines not in the list -- I would love to hear your opinions.\n" +
                "EDIT: As for indexing needs, as users keep entering data into the site, those data would need to be indexed continuously. It doesn't have to be real time " +
                ", but ideally new data would show up in index with no more than 15 - 30 minutes delay";
        List<PhraseInfo> phrases = extractor.parseSentence(sentence);
        for (PhraseInfo phrase : phrases){
            System.out.println("*****[phrase]*****");
            System.out.println(phrase.getText());
            System.out.println(phrase.getSyntaxTree());
            System.out.println(phrase.printProofs());
        }
    }
}
