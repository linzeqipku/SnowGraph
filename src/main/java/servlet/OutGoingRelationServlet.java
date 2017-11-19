package servlet;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by Administrator on 2017/5/26.
 */
public class OutGoingRelationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
        String id = request.getParameter("id");
        //System.out.println("OutGoing: "+id);
        
        List<JSONObject> list = new ArrayList<>();
        Map<String,Integer> cnt = new HashMap<>();

        try (Statement statement = Config.getNeo4jBoltConnection().createStatement()) {
        	String stat="match p=(n)-[r]-(x) where id(r)="+id+" return r";
        	ResultSet rs=statement.executeQuery(stat);
        	while (rs.next()){
        		JSONObject jsobj=new JSONObject((Map)rs.getObject("r"));
        		list.add(jsobj);
        		String key = jsobj.getString("type");
                if (cnt.containsKey(key)){
                    cnt.put(key,cnt.get(key)+1);
                }else cnt.put(key,1);
        	}
        } catch (SQLException e){
        	e.printStackTrace();
        }
        Object [] tmp = cnt.keySet().toArray();
        for (int i = 0; i < tmp.length; i++){
            for (int j = i+1; j < tmp.length; j++){
                int t1 = cnt.get(tmp[i].toString());
                int t2 = cnt.get(tmp[j].toString());
                if (t2 < t1) {
                    Object tt = tmp[i];
                    tmp[i] = tmp[j];
                    tmp[j] = tt;
                }
            }
        }
        JSONArray rejsarr = new JSONArray();
        for (Object key : tmp){
            String k = (String) key;
            for (JSONObject obj : list){
                if (obj.getString("type").equals(k)) {
                    String flag = "in_";
                    if (obj.getString("startId").equals(id)) flag = "ou_";
                    if (flag.equals("in_")) continue;
                    obj.put("type",flag+k);
                    rejsarr.put(obj);
                }
            }
            for (JSONObject obj : list){
                if (obj.getString("type").equals(k)) {
                    String flag = "in_";
                    if (obj.getString("start").equals(id)) flag = "ou_";
                    if (flag.equals("ou_")) continue;
                    obj.put("type",flag+k);
                    rejsarr.put(obj);
                }
            }
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(rejsarr.toString());
    }

}
