package utils;

import java.io.IOException;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

public class ConnectionManager {
	private static PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
	static {
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(20);
	}
	private static CloseableHttpClient httpclient = HttpClients.custom().setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy()).build();
	
	private static void getNewClient() {
		httpclient = HttpClients.custom().setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy()).build();
	}
	
	public static void closeConnection() {
		try {
			httpclient.close();
		} catch (IOException e) {
		}
	}
	
	public static String post(String url, String jsonString) {
		
		HttpPost httpPost = new HttpPost(url);
		StringEntity requestEntity = new StringEntity(jsonString, Consts.UTF_8);
		requestEntity.setChunked(false);
		httpPost.setEntity(requestEntity);
		CloseableHttpResponse response = null;

		try {
			try {
				response = httpclient.execute(httpPost);
			} catch (IllegalStateException e) {
				getNewClient();
				response = httpclient.execute(httpPost);
			} 
		    HttpEntity entity = response.getEntity();
		    String result = response.getStatusLine().getStatusCode() + "|" + EntityUtils.toString(entity);
			response.close();
		    return result;
		    
		} catch (ClientProtocolException e) {
			return null;
		} catch (IOException e) {
			return null;
		} 
	}
	
	public static String get(String url) {
		HttpGet get = new HttpGet(url);
		CloseableHttpResponse response = null;
		try {
			try {
				response = httpclient.execute(get);
			} catch (IllegalStateException e) {
				getNewClient();
			}
		    System.out.println(response.getStatusLine());
		    HttpEntity entity = response.getEntity();
		    String result = EntityUtils.toString(entity);
			response.close();
		    return result;
		    
		} catch (ClientProtocolException e) {
			return null;
		} catch (IOException e) {
			return null;
		} 
	}
}
