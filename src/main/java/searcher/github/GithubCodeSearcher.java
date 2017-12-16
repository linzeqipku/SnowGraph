package searcher.github;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class GithubCodeSearcher {

    private final String accessToken;

    public enum RETURN_MODE {
            CONTENT, URL
    }

    public GithubCodeSearcher(String accessToken) {
        this.accessToken = accessToken;
    }

    public static void main(String[] args){
        GithubCodeSearcher githubCodeSearcher=new GithubCodeSearcher("b0603b617a6cb24a447a308ad71f95a4e5c87783");
        githubCodeSearcher.searchAndSave("IndexReader IndexWriter", "E:/test/githubapi");
    }

    public void searchAndSave(String query, String dirPath){
        List<String> resultList=search(query, RETURN_MODE.CONTENT);
        for (int i=0;i<resultList.size();i++) {
            File file=new File(dirPath + "/" + i + ".java");
            try {
                FileUtils.write(file, resultList.get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> search (String query, RETURN_MODE mode){
        List<String> keywords=convertStringToKeywords(query);
        int K=1;
        List<String> r=new ArrayList<>();
        for (int i=1;i<=K;i++)
            try {
                r.addAll(search(keywords,i,mode));
            } catch (IOException e) {
                e.printStackTrace();
            }
        return r;
    }

    private List<String> convertStringToKeywords(String query){
        List<String> r=new ArrayList<>();
        for (String word:query.split("\\W+"))
            r.add(word);
        return r;
    }

    public List<String> search(List<String> keywords, int pageNum, RETURN_MODE mode) throws IOException {
        String url = "https://api.github.com/search/code?page=" + pageNum +
                "&per_page=100&access_token=" + accessToken + "&q=language:Java+" + StringUtils.join(keywords, "+");
        return searchByUrl(url,mode);
    }

    public List<String> searchByUrl(String url, RETURN_MODE mode) throws IOException {
        List<String> r=new ArrayList<>();
        String str=Request.Get(url).connectTimeout(10000).socketTimeout(10000).execute().returnContent().asString();
        if (str==null)
            return new ArrayList<>();

        ExecutorService pool = Executors.newFixedThreadPool(100);
        List<Future> futureList=new ArrayList<>();

        JSONObject jsonObject = new JSONObject(str);
        JSONArray items=jsonObject.getJSONArray("items");
        Iterator<Object> itemIterator=items.iterator();
        while (itemIterator.hasNext()){
            Object itemObject=itemIterator.next();
            if (!(itemObject instanceof JSONObject))
                continue;
            JSONObject item=(JSONObject)itemObject;
            String itemUrlStr=item.getString("html_url");
            itemUrlStr=itemUrlStr.replace("https://github.com/","https://raw.githubusercontent.com/");
            itemUrlStr=itemUrlStr.replace("/blob/","/");
            if (mode==RETURN_MODE.URL)
                r.add(itemUrlStr);
            else {
                Future f = pool.submit(new HtmlCrawler(itemUrlStr));
                futureList.add(f);
            }
        }
        pool.shutdown();
        for (Future f:futureList)
            try {
                r.add(f.get().toString());
            } catch (InterruptedException|ExecutionException e) {
                e.printStackTrace();
            }

        return r;
    }

    class HtmlCrawler implements Callable<String> {

        private final String url;

        HtmlCrawler(String url) {
            this.url = url;
        }

        @Override
        public String call() throws Exception {
            try {
                return Request.Get(url)
                        .connectTimeout(10000)
                        .socketTimeout(10000)
                        .execute().returnContent().asString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
