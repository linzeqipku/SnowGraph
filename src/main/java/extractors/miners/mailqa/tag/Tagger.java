package extractors.miners.mailqa.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.tagger.maxent.TTags;

/**
 * @ClassName: Tagger
 * @Description: TODO 
 * @author: left
 * @date: 2014年3月12日 上午10:17:40
 */

public class Tagger {

	// Initialize the tagger
	public static MaxentTagger	tagger	= new MaxentTagger(
												"models/wsj-0-18-bidirectional-nodistsim.tagger");

	public static String getTaggedString(String text) {
		String result = "";
		text = text.toLowerCase();
		result = tagger.tagString(text);
		return result;
	}

	public ArrayList<String> getTagVectorOfText(String text) {
		return getTagVector(getTaggedString(text));
	}

	public ArrayList<String> getTagVector(String tagged) {
		ArrayList<String> result = new ArrayList<String>();

		String[] words = tagged.trim().split("\\s+");

		for (String word : words) {
			if (word.indexOf("_") == -1)
				continue;
			String pre = word.substring(0, word.lastIndexOf('_'));
			String post = word.substring(word.lastIndexOf('_') + 1);

			Keywords ky = new Keywords();
			if (ky.isKeywords(pre) || ky.isKeywords(pre.toLowerCase())) {
				result.add(pre);
			}
			else {
				result.add(post);
			}
		}

		return result;
	}

	public ArrayList<Integer> getVector(String text) {
		ArrayList<String> textVector = getTagVectorOfText(text);
		ArrayList<Integer> vector = new ArrayList<Integer>();

		HashMap<String, Integer> codeMap = TagEncoder.getCodeMap();
		for (String word : textVector) {
			if (codeMap.containsKey(word)) {
				vector.add(codeMap.get(word));
			}
			else
				vector.add(Integer.MAX_VALUE);
		}

		return vector;
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		String sample = "How to get the \"context\" of the searched word?";
		ArrayList<String> result = new Tagger().getTagVector(getTaggedString(sample));
		System.out.println(getTaggedString(sample));
		for (String str : result) {
			System.out.print(str + " ");
		}
		System.out.println();

		ArrayList<Integer> result2 = new Tagger().getVector(sample);
		for (Integer i : result2) {
			System.out.print(i + " ");
		}
		System.out.println();
		HashMap<Integer, String> tMap = TagEncoder.getTagMap();
		for (Integer i : result2) {
			System.out.print(tMap.get(i) + " ");
		}
		// // Initialize the tagger
		// MaxentTagger tagger = new
		// MaxentTagger("models/wsj-0-18-bidirectional-nodistsim.tagger");
		//
		// // The sample string
		// String sample =
		// "How do I combine two Analyzers in one QueryParser, where one Analyzer works for some fields and another one for another fields.";
		//
		// // The tagged string
		// String tagged = tagger.tagString(sample);
		//
		// System.out.println();
		// // Output the result
		// System.out.println(tagged);

	}
}
