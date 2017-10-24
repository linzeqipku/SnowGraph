package graphlocater;

import graphlocater.filters.*;
import graphlocater.wrapper.PhraseInfo;
import org.apache.log4j.Logger;


public class PhraseFilter {
	public static final Logger logger = Logger.getLogger(PhraseFilter.class);

	public static void filter(PhraseInfo phrase, String sentence) {
		// logger.info("[FilterPhrase] filtering...");

		if (phrase.getPhraseType() == PhraseInfo.PHRASE_TYPE_VP)
			CheckerPhraseForm.checkVP(phrase);
		else if (phrase.getPhraseType() == PhraseInfo.PHRASE_TYPE_NP)
			CheckerPhraseForm.checkNP(phrase);

		FilterBeVerb.filterInRootOnly(phrase);
		FilterModalVerb.filterInRootOnly(phrase);

		FilterNegation.filterInRootOnly(phrase);
		FilterNegation.filterThoroughly(phrase);
		FilterPronoun.filter(phrase);

		FilterVerb.filter(phrase);

		FilterNoun filterNoun = new FilterNoun(phrase);
		filterNoun.filter();

		FilterPhrase.filter(phrase);

		FilterContext filterContext = new FilterContext(phrase, sentence);
		filterContext.filter();

	}
}
