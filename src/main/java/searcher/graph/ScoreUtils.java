package searcher.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScoreUtils {
    public static final double WORDVEC_SIM_THRESHOLD = 0.5;
    private static Map<String, double[]> word2VecMap = new HashMap<>();

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
                if (maxSim < ScoreUtils.WORDVEC_SIM_THRESHOLD) // filter small word sim below threshold
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
}
