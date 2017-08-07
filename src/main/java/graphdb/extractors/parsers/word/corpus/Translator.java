package graphdb.extractors.parsers.word.corpus;

import graphdb.extractors.parsers.word.utils.Config;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Created by maxkibble on 2017/7/9.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translator {

    public static String lookUp(String word) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        word = word.replaceAll(" ","+");

        //根据查找单词构造查找地址
        HttpGet getWordMean = new HttpGet("http://dict.youdao.com/search?q=" + word + "&keyfrom=dict.index");
        CloseableHttpResponse response = httpClient.execute(getWordMean);//取得返回的网页源码

        String result = EntityUtils.toString(response.getEntity());
        response.close();
        //注意(?s)，意思是让'.'匹配换行符，默认情况下不匹配
        Pattern searchMeanPattern = Pattern.compile("(?s)<div class=\"trans-container\">.*?<ul>.*?</div>");
        Matcher m1 = searchMeanPattern.matcher(result); //m1是获取包含翻译的整个<div>的
        StringBuilder ret = new StringBuilder();

        if (m1.find()) {
            String means = m1.group();//所有解释，包含网页标签
            Pattern getChinese = Pattern.compile("(?m)<li>(.*?)</li>"); //(?m)代表按行匹配
            Matcher m2 = getChinese.matcher(means);

            //System.out.println("释义:");
            while (m2.find()) {
                //在Java中(.*?)是第1组，所以用group(1)
                //System.out.println("\t" + m2.group(1));
                ret.append("\t" + m2.group(1));
            }
        } else {
            //System.out.println("未查找到释义.");
            ret.append("未查找到释义.");
        }
        return ret.toString();
    }

    public static void main(String[] args) throws IOException {
        List<String> lines=
                //FileUtils.readLines(new File(Config.getApiTokensPath()));
                FileUtils.readLines(new File(Config.getProjectApiTokenPath()));
        int tot = 0, errCnt = 0;
        StringBuilder toPrint = new StringBuilder();
        for (String line : lines) {
            tot++;
            String result =  lookUp(line);

            if(result.equals("未查找到释义.")) {
                errCnt++;
                System.out.println(line);
                continue;
            }
            toPrint.append(line + " ");
            String[] tokens = result.split("；|，| ");
            for(String token : tokens) {
                token = token.replaceAll(" |\t|\\.|\\w|\\(.*\\)|（.*）|（.*|.*）|人名", "");
                if(!token.equals("")) toPrint.append(token + " ");
            }
            toPrint.append("\n");
        }

        FileOutputStream fout = new FileOutputStream(Config.getProjectTranslationPath());
        fout.write(toPrint.toString().getBytes());
        System.out.println("TOTAL TOKENS: " + tot);
        System.out.println("TOKENS FAILED TO TRANSLATE: " + errCnt);
    }
}