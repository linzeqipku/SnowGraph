package graphdb.extractors.parsers.word.corpus;

import graphdb.extractors.parsers.word.utils.Config;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Dictionary {
    private static HashMap<String, String> dic;
    private int hitCacheNum = 0;
    private int totQueryNum = 0;

    public void init() throws IOException {
        dic = new HashMap<>();
        List<String> lines =
                FileUtils.readLines(new File(Config.getProjectTranslationPath()));
        for(String line : lines) {
            int idx = line.indexOf(' ');
            String key = line.substring(0, idx);
            String value = line.substring(idx + 1, line.length());
            dic.put(key, value);
        }
    }

    public ArrayList<String> getTranslation(String word) {
        ArrayList<String> ret = new ArrayList<>();
        totQueryNum++;
        if(dic == null) {
            System.out.println("Shall not see this");
            return ret;
        }
        if(dic.containsKey(word)) {
            hitCacheNum++;
            String translation = dic.get(word);
            String[] exps = translation.split(" ");
            for(String exp : exps) ret.add(exp);
            return ret;
        }
        try {
            String result = Translator.lookUp(word);
            if(result.equals("未查找到释义.")) return ret;
            StringBuilder toPrint = new StringBuilder();
            String[] tokens = result.split("；|，| ");
            for(String token : tokens) {
                token = token.replaceAll(
                        " |\t|\\.|[(A-Za-z)]|\\(.*\\)|\\[.*]|（.*）|（.*|.*）", "");
                toPrint.append(token + " ");
                ret.add(token);
            }
            dic.put(word, toPrint.toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void printQuery() {
        System.out.println("Total Query Times: " + totQueryNum + "\nHit Cache Times: " + hitCacheNum);
    }

}
