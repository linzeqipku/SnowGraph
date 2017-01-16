package extractors.miners.mailqa.tag;

import java.util.HashSet;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.tagger.maxent.TTags;

public class TagWords {

	public static HashSet<String>	tagWords	= new HashSet<String>();

	public static HashSet<String> getTagWords() {
		if (tagWords.size() == 0) {
			MaxentTagger tagger = new MaxentTagger("models/wsj-0-18-bidirectional-nodistsim.tagger");
			TTags tags = tagger.getTags();
			for (int i = 0; i < tags.getSize(); i++) {
				tagWords.add(tags.getTag(i));
			}
		}
		return tagWords;
	}
}
