package exps.graphlocater.utils;

import webapp.SnowGraphContext;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import exps.graphlocater.VPExtractor;
import exps.graphlocater.wrapper.PhraseInfo;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by laurence on 17-10-26.
 */
public class WordsUtils {
    private static EnglishStemmer stemmer = new EnglishStemmer();
    private static StanfordCoreNLP pipeline = null;
    private static Set<String> englishStopWords = new HashSet<>();
    private static Map<String, double[]> word2VecMap = new HashMap<>();
    private static ILexicalDatabase db = new NictWordNet();
    private static boolean debug = true;
    private static double MIN_WORDVEC_SIM = 0.25;

    static{
        initPipeline();
        loadStopWors();
        loadWordVec();
    }

    private static void initPipeline(){
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);
    }

    private static void loadWordVec(){
        try{
            Scanner scanner = new Scanner(new FileInputStream("C:\\Users\\Ling\\Documents\\glove.6B\\glove.6B.100d.txt"));
            while(scanner.hasNext()) {
                String[] line = scanner.nextLine().trim().split(" ");
                String word = line[0];
                if (word.length() <= 2 || englishStopWords.contains(word)) {
                    continue;
                }

                double[] vec = new double[100];
                for (int i = 1; i < line.length; ++i){
                    vec[i-1] = Double.parseDouble(line[i]);
                }
                word2VecMap.put(word, vec);
            }
            System.out.println("word2vec map size: " + word2VecMap.size());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void loadStopWors(){
        List<String> lines=new ArrayList<>();
		try {
			lines= FileUtils.readLines(new File(SnowGraphContext.class.getResource("/").getPath()+"stopwords_lcy.txt"));
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

    private static Set<String> lemmatize(Set<String> wordSet){
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

    public static double getWordSetSim(Set<String> orgTgtSet, Set<String> orgDescSet){
        double TP = 0;
        int R = orgTgtSet.size(), P = orgDescSet.size();

        // intersection of the two set
        Set<String> matchedSet = new HashSet<>();
        matchedSet.addAll(orgTgtSet);
        matchedSet.retainAll(orgDescSet);
        TP += matchedSet.size();

        // remove intersection, do not change original set
        Set<String> tgtSet = new HashSet<>();
        tgtSet.addAll(orgTgtSet);
        tgtSet.removeAll(matchedSet);
        Set<String> descSet = new HashSet<>();
        descSet.addAll(orgDescSet);
        descSet.removeAll(matchedSet);

        // for each word in desc set, find the match word with max similarity
        Map<String, Double> recallMap = new HashMap<>();
        for (String desc: descSet){
            double maxSim = 0;
            String matchedWord = "";
            for (String word: tgtSet){
                double curSim = getSingleWordSimWord2Vec(desc, word);
                if (curSim > maxSim){
                    maxSim = curSim;
                    matchedWord = word;
                }
            }
            if (maxSim < WordsUtils.MIN_WORDVEC_SIM) // filter small word sim below threshold
                continue;
            TP += maxSim;
            Double preVal = recallMap.get(matchedWord);
            if (preVal != null){
                double curVal = Math.min(preVal + maxSim, 1.0);
                recallMap.put(matchedWord, curVal);
            } else {
                recallMap.put(matchedWord, maxSim);
            }

        }
        double precision = TP / P;
        double recall = 0;
        for(String key: recallMap.keySet()){
            recall += recallMap.get(key);
        }
        recall = (recall + matchedSet.size()) / R;
        double score = 2 * precision * recall / (precision + recall);
        return score;
    }

    private static double getSingleWordSimWord2Vec(String w1, String w2) {
        double[] v1 = word2VecMap.get(w1);
        double[] v2 = word2VecMap.get(w2);
        if (v1 == null || v2 == null)
            return 0;
        double product = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < v1.length; ++i){
            product += v1[i] * v2[i];
            normA += v1[i] * v1[i];
            normB += v2[i] * v2[i];
        }
        return product / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public static double getSingleWordSimWS4J(String word1, String word2){
        /*available options of metrics
	        private static RelatednessCalculator[] rcs = { new HirstStOnge(db),
			    new LeacockChodorow(db), new Lesk(db), new WuPalmer(db),
			    new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db) };
	    */
        RelatednessCalculator rc1 = new WuPalmer(db);
        double sim = rc1.calcRelatednessOfWords(word1, word2);
        return sim;
    }

    /*public static void main(String[] args){
        double s = getSingleWordSimWord2Vec("add", "remove");
        System.out.println(s);
    }*/
}
