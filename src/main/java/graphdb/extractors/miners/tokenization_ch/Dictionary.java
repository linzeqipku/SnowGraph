package graphdb.extractors.miners.tokenization_ch;

import graphdb.extractors.parsers.word.corpus.Translator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Dictionary {
    private static HashMap<String, String> dic = new HashMap<>();
    private int hitCacheNum = 0;
    private int totQueryNum = 0;

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
            String result = Translator.en2ch(word);
            if(result.equals("未查找到释义.")) return ret;
            StringBuilder toPrint = new StringBuilder();
            String[] tokens = result.split("；|，| ");
            for(String token : tokens) {
                token = token.replaceAll(
                        " |\t|\\.|\\w|\\([^)]*\\)|\\[[^]]*]|（[^）]*）|（.*|.*）|人名", "");
                if(!token.equals("")) toPrint.append(token + " ");
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
