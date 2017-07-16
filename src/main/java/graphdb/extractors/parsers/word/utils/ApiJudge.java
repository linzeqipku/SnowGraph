package graphdb.extractors.parsers.word.utils;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by maxkibble on 2017/5/25.
 */
public class ApiJudge {

    public static HashSet<String> globalApiList = new HashSet<>();
    public static HashSet<String> globalPackageList = new HashSet<>();
    public static HashSet<String> apiStopWords = new HashSet<>();
    public static Logger logger = Logger.getLogger(ApiJudge.class);

    public static boolean isLetter(char c) {
        if(c >= 'A' && c <= 'Z') return true;
        if(c >= 'a' && c <= 'z') return true;
        return false;
    }

    public static boolean isCamelCase(String ele) {
        int len = ele.length();
        if(len == 0) {
            return false;
        }
//        else if(len == 1) {
//            char c = ele.charAt(0);
//            if(isLetter(c)) return true;
//            else return false;
//        }
//        else {
//            if(!isLetter(ele.charAt(0))) {
//                return false;
//            }
//            for(int i = 1; i < len; i++) {
//                char c1 = ele.charAt(i - 1);
//                char c2 = ele.charAt(i);
//                if(!isLetter(c2)) {
//                    return false;
//                }
//                if(c1 >= 'A' && c1 <= 'Z' &&
//                        c2 >= 'A' && c2 <= 'Z') {
//                    return false;
//                }
//            }
//        }
        for(int i = 0; i < len; i++) {
            if(!isLetter(ele.charAt(i))) return false;
        }
        return true;
    }

    public static boolean isApi(String word) {
        try {
            loadStopWords();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        for(String stopword : apiStopWords) {
            if(word.equals(stopword)) return false;
        }
        String[] wordEle = word.split("\\.");
        for(String ele : wordEle) {
            if(!isCamelCase(ele)) return false;
        }
        return true;
    }

    public static void loadStopWords() throws IOException {
        List<String> lines= FileUtils.readLines(new File(Config.getApiStopWordPath()));
        for (String line:lines)
            apiStopWords.add(line);
    }

    public static boolean isPackageName(String word) {
        if(!isApi(word)) return false;
        if(word.contains(".")) return true;
        return false;
    }

    public static boolean isProjectName(String word) {
        int len = word.length();
        for(int i = 0; i < len; i++) {
            char c = word.charAt(i);
            if(!(isLetter(c) || c == '-' || c == '_'))
                return false;
        }
        return true;
    }

    public static ArrayList<String> splitCamelCase(String word) {
        ArrayList<String> ret = new ArrayList<>();
        int len = word.length();
        StringBuilder cur = new StringBuilder();
        for(int i = 0; i < len; i++) {
            if (!isLetter(word.charAt(i))) {
                logger.info("驼峰分词时有非字母元素");
                return ret;
            }
            char c1 = word.charAt(i);
            if (i == len - 1) {
                if (c1 >= 'A' && c1 <= 'Z' && i != 0 && word.charAt(i - 1) >= 'a' && word.charAt(i - 1) <= 'z') {
                    if(!cur.equals("")) ret.add(cur.toString());
                    cur = new StringBuilder();
                    cur.append(word.charAt(i));
                    ret.add(cur.toString());
                } else {
                    cur.append(word.charAt(i));
                    ret.add(cur.toString());
                }
                continue;
            }
            char c2 = word.charAt(i + 1);
            if (i == 0) {
                cur.append(word.charAt(i));
                continue;
            }
            char c0 = word.charAt(i - 1);
            if((c1 >= 'A' && c1 <= 'Z') && ((c0 >= 'a' && c0 <= 'z') || (c2 >= 'a' && c2 <= 'z'))) {
                ret.add(cur.toString());
                cur = new StringBuilder();
                cur.append(word.charAt(i));
            }
            else cur.append(word.charAt(i));
        }
        return ret;
    }

    public static ArrayList<String> lookUpProjectDictionary(String apiName) {
        ArrayList<String> tokens = ApiJudge.splitCamelCase(apiName);
        ArrayList<String> chineseTokens = new ArrayList<>();
        try {
            List<String> translations = FileUtils.readLines(new File(Config.getProjectTranslationPath()));
            for(String token : tokens) {
                for(String trans : translations) {
                    String[] tokensCh = trans.split(" ");
                    if(!token.equals(tokensCh[0])) continue;
                    for(int i = 1; i < tokensCh.length; i++) chineseTokens.add(tokensCh[i]);
                }
            }
        }
        catch (IOException e) {
            System.out.println("构建代码图时需要对API进行驼峰切词和翻译，请确保已将翻译结果存在指定路径下");
            e.printStackTrace();
        }
        return chineseTokens;
    }

    public static void main(String[] args) {
        String[] test = {"A", "a", "SendMessage", "sendSMSMessage", "SMS", "SMSSender", "senderSMS"};
        for(String str : test) {
            ArrayList<String> tokens = splitCamelCase(str);
            for(String token : tokens) System.out.print(token + " ");
            System.out.println();
        }
    }
}
