package servlet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Administrator on 2017/5/26.
 */
public class RandomServlet extends HttpServlet {

	Random rand ;
	Map<Integer, Pair<Long,Long>> map = new HashMap<>();
	
	public void init(ServletConfig config) throws ServletException{
		
		rand = new Random();

        /* 读取数据 */
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(Config.getExampleFilePath())),
                                                                         "UTF-8"));
            String lineTxt;
            while ((lineTxt = br.readLine()) != null) {
            	if (lineTxt.length()==0)
            		continue;
                String[] names = lineTxt.split(" ");
                map.put(Integer.parseInt(names[0]), new ImmutablePair<>(Long.parseLong(names[1]), Long.parseLong(names[2])));
            }
            br.close();
        } catch (Exception e) {
            System.err.println("read errors :" + e);
        }		
	}
	
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        long id = rand.nextInt(map.size());
        String query = Config.getDocSearcher().getQuery(map.get(id).getLeft());
        JSONObject searchResult = new JSONObject();
        searchResult.put("query", query);
        searchResult.put("query2", query);
        searchResult.put("answerId", map.get(id).getRight());

        response.getWriter().print(searchResult.toString());
    }

}