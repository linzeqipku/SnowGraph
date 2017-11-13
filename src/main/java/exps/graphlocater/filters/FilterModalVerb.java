package exps.graphlocater.filters;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import exps.graphlocater.wrapper.PhraseInfo;
import exps.graphlocater.wrapper.Proof;
import exps.graphlocater.wrapper.ProofType;


public class FilterModalVerb {

	public static boolean filterInRootOnly(PhraseInfo phrase) {
		if (phrase == null)
			return false;
		Tree phraseTree = Tree.valueOf(phrase.getSyntaxTree());
		if (phraseTree == null)
			return false;

		StringBuilder filterPattern = new StringBuilder("__ < MD=md");

		TregexPattern tregexPattern = TregexPattern.compile(filterPattern.toString());
		TregexMatcher matcher = tregexPattern.matcher(phraseTree);

		// 如果按照过滤情态动词的pattern无法匹配，则短语应被过滤掉
		if (matcher.matches()) {
			Proof proof = new Proof(ProofType.FAIL_MODAL_VERB_ROOT);
			Tree evdTree = matcher.getNode("md");
			proof.setEvidenceTree(evdTree.pennString().trim());
			phrase.addProof(proof);
			return false;
		}

		return true;
	}

	public static boolean filterThoroughly(PhraseInfo phrase) {
		if (phrase == null)
			return false;

		Tree phraseTree = Tree.valueOf(phrase.getSyntaxTree());

		StringBuilder filterPattern = new StringBuilder("__ << MD=md");

		TregexPattern tregexPattern = TregexPattern.compile(filterPattern.toString());
		TregexMatcher matcher = tregexPattern.matcher(phraseTree);

		// 如果按照过滤情态动词的pattern无法匹配，则短语应被过滤掉
		if (matcher.matches()) {
			Proof proof = new Proof(ProofType.FAIL_MODAL_VERB_THOROUGHLY);
			Tree evdTree = matcher.getNode("md");
			proof.setEvidenceTree(evdTree.pennString().trim());
			phrase.addProof(proof);
			return false;
		}

		return true;
	}
}
