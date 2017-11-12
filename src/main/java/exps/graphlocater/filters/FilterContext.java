package exps.graphlocater.filters;

import java.util.Arrays;
import java.util.HashSet;

import exps.graphlocater.wrapper.PhraseInfo;
import exps.graphlocater.wrapper.Proof;
import exps.graphlocater.wrapper.ProofType;

public class FilterContext {
	private PhraseInfo phrase;
	private String					sentence;
	private String					precedingContext;
	private static HashSet<String>	qaContextList;

	static {
		qaContextList = new HashSet<String>();

		qaContextList.addAll(Arrays.asList(Rules.qa_phrases));
		qaContextList.addAll(Arrays.asList(Rules.qa_verbs));
		qaContextList.addAll(Arrays.asList(Rules.qa_nouns));
	}

	public FilterContext(PhraseInfo phrase, String sentence) {
		this.phrase = phrase;
		this.setSentence(sentence);
		this.precedingContext = getPrecedingContext(phrase.getText(), sentence);
		// System.out.println(precedingContext);
	}

	public boolean filter() {
		if (precedingContext == null || "".equals(precedingContext.trim()))
			return false;
		for (String qa : qaContextList) {
			if (precedingContext.endsWith(qa)) {
				Proof proof = new Proof(ProofType.CONTEXT_IMMEDIATE);
				proof.setEvidence(qa);
				phrase.addProof(proof);
			}
		}
		if (phrase.hasProof(ProofType.CONTEXT_IMMEDIATE))
			return true;

		for (String qa : qaContextList) {
			qa = qa + " ";// qa关键词后面加一个空格的原因是，避免出现了done这样的词，既识别出done也识别出do
			int idx = precedingContext.indexOf(qa);
			if (idx < 0)
				continue;

			int nearbyThreshold = 12;
			if (idx + qa.length() + nearbyThreshold >= precedingContext.length()) {
				Proof proof = new Proof(ProofType.CONTEXT_NEARBY);
				proof.setEvidence(qa);
				phrase.addProof(proof);
			}
		}
		if (phrase.hasProof(ProofType.CONTEXT_NEARBY))
			return true;

		for (String qa : qaContextList) {
			int precedingThreshold = 50;
			int idx = precedingContext.indexOf(qa);
			if (idx < 0)
				continue;
			if (idx + qa.length() + precedingThreshold >= precedingContext.length()) {
				Proof proof = new Proof(ProofType.CONTEXT_PRECEDING);
				proof.setEvidence(qa);
				phrase.addProof(proof);
			}
		}
		if (phrase.hasProof(ProofType.CONTEXT_PRECEDING))
			return true;
		else
			return false;
	}

	public static String getPrecedingContext(String phrase, String context) {
		// System.out.println(context + "\t" + phrase);
		if (phrase == null || context == null)
			return null;
		int i = context.indexOf(phrase);
		// System.out.println(i);
		if (i == -1)
			return null;
		String preceding = context.substring(0, i);
		// System.out.println(preceding);
		return preceding.trim();
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

}
