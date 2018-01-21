package graphdb.extractors.parsers.word.corpus;

import graphdb.extractors.parsers.word.translation.trans.LANG;
import graphdb.extractors.parsers.word.translation.trans.factory.TFactory;
import graphdb.extractors.parsers.word.translation.trans.factory.TranslatorFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Created by maxkibble on 2017/7/9.
 */

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translator {

    public static String ch2en(String content) throws IOException {
        content=content.replace("查询"," query ");
        content=content.replace("验证"," check ");
        content=content.replace("微博"," weibo ");
        content=content.replace("短信"," SMS ");
        content=content.replace("如何","");
        System.out.println(content);
        if(content.length() == 0) return "";

        TFactory factory = null;
        try {
            factory = new TranslatorFactory();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String r=factory.get("google").trans(LANG.ZH, LANG.EN, content);
            System.out.println(r);
            return r;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}