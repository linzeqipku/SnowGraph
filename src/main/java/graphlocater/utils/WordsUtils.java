package graphlocater.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by laurence on 17-10-26.
 */
public class WordsUtils {

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
                    result.add(buffer.toString());
                    buffer.delete(0, buffer.length());
                }
                result.add(element);
            }
        }
        if (buffer.length() > 0)
            result.add(buffer.toString());
        for (String word : result){
            System.out.println(word + " " + word.toLowerCase());
        }
        return result;
    }

    public static double getWordSetSim(Set<String> wordSet, Set<String>descSet){
        int TP = 0;
        for (String desc: descSet){
            if (wordSet.contains(desc))
                TP++;
        }
        double precision = TP * 1.0 / descSet.size();
        double recall = TP * 1.0 / wordSet.size();
        double score = 2 * precision * recall / (precision + recall);
        return score;
    }
}
