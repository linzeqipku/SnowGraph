package graphlocater.utils;

import cn.edu.pku.sei.SnowView.servlet.Config;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import graphlocater.VPExtractor;
import graphlocater.wrapper.PhraseInfo;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by laurence on 17-10-26.
 */
public class WordsUtils {
    public static EnglishStemmer stemmer = new EnglishStemmer();
    public static StanfordCoreNLP pipeline = null;
    public static Set<String> englishStopWords = new HashSet<>();
    public static ILexicalDatabase db = new NictWordNet();
     public static boolean debug = true;

    static{
        initPipeline();
        loadStopWors();
    }

    public static void initPipeline(){
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);

    }

    public static void loadStopWors(){
        List<String> lines=new ArrayList<>();
		try {
			lines= FileUtils.readLines(new File(Config.class.getResource("/").getPath()+"stopwords_lcy.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		englishStopWords.addAll(lines);
		lines.forEach(n->{
			stemmer.setCurrent(n);
			stemmer.stem();
			englishStopWords.add(stemmer.getCurrent());
		});
    }

    public static Set<String> lemmaAndRemoveStopWords(Set<String> wordSet){
        Set<String> res = new HashSet<>();
        String text = String.join(" ", wordSet);
        Annotation document = new Annotation(text);
        // run all Annotators on this text
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String lemma = token.lemma();
                if (!englishStopWords.contains(lemma)){
                    res.add(lemma);
                }
            }
        }
        return res;
    }

    public static Set<String> lemmatize(Set<String> wordSet){
        Set<String> res = new HashSet<>();
        String text = String.join(" ", wordSet);
        Annotation document = new Annotation(text);
        // run all Annotators on this text
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String lemma = token.lemma();
                res.add(lemma);
            }
        }
        return res;
    }

    public static List<PhraseInfo> extractPhraseFromQuery(String query){
        List<PhraseInfo> phrases = new ArrayList<>();
        String cleanText = Jsoup.parse("<html>" + query + "</html>").text();
        Annotation document = new Annotation(cleanText);
        // run all Annotators on this text
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence: sentences) {
            List<PhraseInfo> current = VPExtractor.parseSentence(sentence.toString());
            if (current == null)
                continue;
            phrases.addAll(current);
            if (debug){
                for (PhraseInfo phrase : current) {
                    System.out.println("*****[phrase]*****");
                    System.out.println(phrase.getText());
                    System.out.println(phrase.getProofScore());
                    System.out.println(phrase.getSyntaxTree());
                    System.out.println(phrase.getCleanWordSet().toString());
                }
            }
        }

        return phrases;
    }

    public static Set<String> tokenizeCodeNames(String name){
        Set<String> result = new HashSet<>();

        if (name.matches("[a-zA-Z]*\\d+[a-zA-Z]*\\d*")){ // if contains digit
            name = name.replaceAll("\\d+", " ").trim();
            for (String word : name.split("\\s+")){
                result.add(word.toLowerCase());
            }
            return result;
        }
        if (name.contains("_")){ // if contains underscore
            for (String word : name.split("_+")){
                if (word.trim().length() > 1){
                    result.add(word.toLowerCase());
                }
            }
            return result;
        }

        List<Integer> upperPos = new ArrayList<>();
        List<String> seq = new ArrayList<>();
        upperPos.add(0);
        for (int i = 0; i < name.length(); ++i){
            if (Character.isUpperCase(name.charAt(i)))
                upperPos.add(i);
        }
        upperPos.add(name.length());
        for (int i = 0; i < upperPos.size()-1; ++i){
            String sub = name.substring(upperPos.get(i), upperPos.get(i+1)); // may contain ""
            if (!sub.equals(""))
                seq.add(sub);
        }

        StringBuffer buffer = new StringBuffer();
        for (String element : seq){
            if (element.length() == 1 && Character.isUpperCase(element.charAt(0))){
                buffer.append(element);
            }else{
                if (buffer.length() > 0) {
                    result.add(buffer.toString().toLowerCase());
                    buffer.delete(0, buffer.length());
                }
                result.add(element.toLowerCase());
            }
        }
        if (buffer.length() > 0)
            result.add(buffer.toString().toLowerCase());

        return lemmatize(result);
    }

    public static double getWordSetSimNaive(Set<String> wordSet, Set<String>descSet){
        int TP = 0;
        for (String desc: descSet){
            if (wordSet.contains(desc)) {
                TP++;
            }
        }
        double precision = TP * 1.0 / descSet.size();
        double recall = TP * 1.0 / wordSet.size();
        double score = 2 * precision * recall / (precision + recall);
        return score;
    }

    public static double getWordSetSim(Set<String> tgtSet, Set<String>descSet){
        double TP = 0;
        Map<String, Double> recallMap = new HashMap<>();
        for (String desc: descSet){
            if (tgtSet.contains(desc)) {
                TP++;
            } else {
                double maxSim = 0;
                String matchedWord = "";
                for (String word: tgtSet){
                    double curSim = getSingleWordSim(desc, word);
                    if (curSim > maxSim){
                        maxSim = curSim;
                        matchedWord = word;
                    }
                }
                TP += maxSim;
                Double preVal = recallMap.get(matchedWord);
                if (preVal != null){
                    double curVal = Math.min(preVal + maxSim, 1.0);
                    recallMap.put(matchedWord, curVal);
                } else {
                    recallMap.put(matchedWord, maxSim);
                }
            }
        }
        double precision = TP / descSet.size();
        double recall = 0;
        for(String key: recallMap.keySet()){
            recall += recallMap.get(key);
        }
        recall /= tgtSet.size();
        double score = 5 * precision * recall / (precision + 4 * recall);
        return score;
    }

    public static double getSingleWordSim(String word1, String word2){
        /*available options of metrics
	        private static RelatednessCalculator[] rcs = { new HirstStOnge(db),
			    new LeacockChodorow(db), new Lesk(db), new WuPalmer(db),
			    new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db) };
	    */
        RelatednessCalculator rc1 = new WuPalmer(db);
        double sim = rc1.calcRelatednessOfWords(word1, word2);
        //System.out.println(word1 + " " + word2 + " " + sim);
        return sim;
    }

    /*public static void main(String[] args){
        getSingleWordSim("delete", "remove");
    }*/
}
