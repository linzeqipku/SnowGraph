package graphdb.extractors.parsers.word.corpus;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by maxkibble on 2017/7/9.
 */

import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translator {

    public static String en2ch(String word) throws IOException {
        String regexCh = "(?m)<li>(.*?)</li>";

        CloseableHttpClient httpClient = HttpClients.createDefault();

        word = word.replaceAll(" ","+");

        HttpGet getWordMean = new HttpGet("http://dict.youdao.com/search?q=" + word + "&keyfrom=dict.index");
        CloseableHttpResponse response = httpClient.execute(getWordMean);//取得返回的网页源码

        String result = EntityUtils.toString(response.getEntity());
        response.close();

        Pattern searchMeanPattern = Pattern.compile("(?s)<div class=\"trans-container\">.*?<ul>.*?</div>");
        Matcher m1 = searchMeanPattern.matcher(result);
        StringBuilder ret = new StringBuilder();

        if (m1.find()) {
            String means = m1.group();

            Pattern getChinese = Pattern.compile(regexCh);
            Matcher m2 = getChinese.matcher(means);

            while (m2.find()) {
                ret.append("\t" + m2.group(1));
            }
        } else {
            ret.append("未查找到释义.");
        }
        return ret.toString();
    }

    public static String ch2en(String content) throws IOException {
        if(content.length() == 0) return "";

        int i;
        for(i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            // make sure the query start with a chinese character
            // chinese character in unicode: 0x4e00-0x9fbb
            if(c >= 0x4e00 && c <= 0x9fbb)
                break;
        }
        if(i == content.length())
                return content;
        content = content.substring(i);


        String url = "http://dict.youdao.com/search?q=" + URLEncoder.encode( content, "UTF-8")  + "&keyfrom=dict.index";
        Document doc = Jsoup.connect(url).timeout(10000).get();
        //System.out.println(url);
        //System.out.println(doc.body());
        Elements links = doc.getElementsByClass("trans-container");
        StringBuilder ret = new StringBuilder();

        //System.out.println(links.size());

        for (Element link : links) {
            String linkText = link.text();
            linkText = linkText.replaceAll( "&#39;", "'" );
            //System.out.println(linkText);
            Pattern searchMeanPattern = Pattern.compile("(?s)[A-Za-z|0-9|\\s|.|,|'|!|:]+");
            Matcher m1 = searchMeanPattern.matcher(linkText);
            while(m1.find())
                ret.append(m1.group(0));
            break;
        }
        if(ret.length() == 0) return "No translation found\n";
        return ret.toString();
    }

    public static void main(String[] args) throws IOException {
/*        List<String> lines=
                //FileUtils.readLines(new File(SnowGraphContext.getApiTokensPath()));
                FileUtils.readLines(new File(SnowGraphContext.getProjectApiTokenPath()));
        int tot = 0, errCnt = 0;
        StringBuilder toPrint = new StringBuilder();
        for (String line : lines) {
            tot++;
            String result =  en2ch(line);

            if(result.equals("未查找到释义.")) {
                errCnt++;
                System.out.println(line);
                continue;
            }
            toPrint.append(line + " ");
            String[] tokens = result.split("；|，| ");
            for(String token : tokens) {
                token = token.replaceAll(" |\t|\\.|\\w|\\([^)]*\\)|\\[[^]]*]|（[^）]*）|（.*|.*）|人名", "");
                if(!token.equals("")) toPrint.append(token + " ");
            }
            toPrint.append("\n");
        }

        FileOutputStream fout = new FileOutputStream(SnowGraphContext.getProjectTranslationPath());
        fout.write(toPrint.toString().getBytes());
        System.out.println("TOTAL TOKENS: " + tot);
        System.out.println("TOKENS FAILED TO TRANSLATE: " + errCnt);*/
        System.out.println(ch2en("你好，世界"));
    }
}