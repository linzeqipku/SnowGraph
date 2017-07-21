package cn.edu.pku.sei.SnowView.utils;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Created by Administrator on 2017/5/26.
 */
public class PostUtil {
    //post请求方法
    public static String sendPost(String url, String data) {
        String response = null;

        try {
            CloseableHttpClient httpclient = null;
            CloseableHttpResponse httpresponse = null;
            try {
                httpclient = HttpClients.createDefault();
                HttpPost httppost = new HttpPost(url);
                StringEntity stringentity = new StringEntity(data,
                        ContentType.create("application/json", "UTF-8"));
                httppost.setEntity(stringentity);
                httppost.setHeader("Accept","application/json; charset=UTF-8");
                System.out.println("connect");
                httpresponse = httpclient.execute(httppost);
                System.out.println("connected");
                response = EntityUtils
                        .toString(httpresponse.getEntity());
            } finally {
                if (httpclient != null) {
                    httpclient.close();
                }
                if (httpresponse != null) {
                    httpresponse.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
    public static String sendGet(String url) {
        String response = null;

        try {
            CloseableHttpClient httpclient = null;
            CloseableHttpResponse httpresponse = null;
            try {
                httpclient = HttpClients.createDefault();
                HttpGet httppost = new HttpGet(url);
                httppost.setHeader("Accept","application/json; charset=UTF-8");
                System.out.println("connect");
                httpresponse = httpclient.execute(httppost);
                System.out.println("connected");
                response = EntityUtils
                        .toString(httpresponse.getEntity());
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
    public static void main(String args[]){
        String p = PostUtil.sendGet("http://neo4j:123@127.0.0.1:7474/db/data/cypher");
        System.out.println(p);
    }
}
