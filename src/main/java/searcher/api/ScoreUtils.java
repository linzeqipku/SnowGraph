package searcher.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ScoreUtils {
    public static final double COUNT_SIM_THRESHOLD = 0.6;
    public static final double CANDIDATE_SIM_THRESHOLD = 0.75;
    private static Map<String, double[]> word2VecMap = new HashMap<>();

    static{
        loadWordVec();
    }
    private static void loadWordVec(){
        try{
            Scanner scanner = new Scanner(new FileInputStream("C:\\Users\\dell\\Documents\\glove\\glove-100d.txt"));
            while(scanner.hasNext()) {
                String[] line = scanner.nextLine().trim().split(" ");
                String word = line[0];
                if (word.length() <= 2) {
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

    // query word set is not stemmed, but id2wrods is stemmed!
    public static Map<Long, Double> getAPISimScore(Set<String>queryWordSet, Map<Long, Set<String> > id2Wrods){

        Map<Long, Double> scoreMap = new HashMap<>();

        for (long id: id2Wrods.keySet()) {
            Set<String> orgDescSet = id2Wrods.get(id);
            double TP = 0;
            int R = queryWordSet.size(), P = orgDescSet.size();

            // intersection of the two set
            Set<String> matchedSet = new HashSet<>();
            matchedSet.addAll(queryWordSet);
            matchedSet.retainAll(orgDescSet);

            TP += matchedSet.size();

            // remove intersection, do not change the original set
            Set<String> tgtSet = new HashSet<>();
            tgtSet.addAll(queryWordSet);
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
                if (maxSim < ScoreUtils.COUNT_SIM_THRESHOLD) // filter small word sim below threshold
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
            // calculate F1 score
            double precision = TP / P;
            double recall = 0;
            for(String key: recallMap.keySet()){
                recall += recallMap.get(key);
            }
            recall = (recall + matchedSet.size()) / R;
            double score = 2 * precision * recall / (precision + recall);

            scoreMap.put(id, score);
        }
        return scoreMap;
    }

    public static void generateCandidateMap(Map<String, Set<Long>> candidateMap, Set<String>queryWordSet, Map<String, Set<Long> > word2Ids) {

        Set<String> dummyWord = new HashSet<>();

        for (String word: queryWordSet){
            Set<Long> nodes = new HashSet<>();
            Set<Long> tmp = word2Ids.get(word);
            if (tmp != null)
                nodes.addAll(tmp);
            for (String key: word2Ids.keySet())
                if (getSingleWordSimWord2Vec(word, key) > ScoreUtils.CANDIDATE_SIM_THRESHOLD)
                    nodes.addAll(word2Ids.get(key));
            if (nodes.size() > 0) {
                if(!candidateMap.containsKey(word))
                    candidateMap.put(word, nodes);
                else
                    candidateMap.get(word).addAll(nodes);
            }
            else // 如果这个词没有任何可以对应的结点，则去掉它，但他可能之前已经对应到一个类的全名
                dummyWord.add(word);
        }
        queryWordSet.removeAll(dummyWord);
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

    public static void main(String[] args){
        System.out.println(getSingleWordSimWord2Vec("get", "find"));
    }
}
