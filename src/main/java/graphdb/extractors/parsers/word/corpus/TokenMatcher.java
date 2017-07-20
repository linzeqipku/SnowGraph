package graphdb.extractors.parsers.word.corpus;

import graphdb.extractors.parsers.word.utils.Config;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxkibble on 2017/7/15.
 */
public class TokenMatcher {
    static List<String> corpusCh;
    static List<String> corpusTrans;

    public static void init() {
        try {
            corpusCh = FileUtils.readLines(new File(Config.getProjectChineseTokenPath()));
            corpusTrans = FileUtils.readLines(new File(Config.getProjectTranslationPath()));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean appear(String word) {
        for(String str : corpusCh) if(word.equals(str)) return true;
        return false;
    }

    public static void main(String[] args) {
        init();
        int tot = 0;
        int hit = 0;
        for(String trans : corpusTrans) {
            boolean flag = false;
            String[] content = trans.split(" ");
            if(content.length == 1) continue;
            for(String ele : content) {
                if(flag) break;
                ArrayList<String> tokens = WordSegmenter.demo(ele);
                for(String token : tokens) if(appear(token)) { flag = true; break; }
            }
            if(flag) hit++;
            tot++;
            if(!flag) System.out.println(trans);
        }
        System.out.println(hit);
        System.out.println(tot);
    }
}
