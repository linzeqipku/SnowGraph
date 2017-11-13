package cn.edu.pku.sei.SnowView.servlet;
import cn.edu.pku.sei.SnowView.utils.PostUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

        String p = PostUtil.sendGet(Config.getUrl()+"/db/data/node/"+id+"/relationships/all");
        JSONArray jsarr = new JSONArray(p);
        Map<String,Integer> cnt = new HashMap<>();
        Iterator<Object> it = jsarr.iterator();
        List<JSONObject> list = new ArrayList<>();

        while (it.hasNext()){
            JSONObject jsobj = (JSONObject)it.next();
            list.add(jsobj);
            String key = jsobj.getString("type");
            if (cnt.containsKey(key)){
                cnt.put(key,cnt.get(key)+1);
            }else cnt.put(key,1);
        }
        Object [] tmp = cnt.keySet().toArray();
        for (int i = 0; i < tmp.length; i++){
            for (int j = i+1; j < tmp.length; j++){
                int t1 = cnt.get((String)tmp[i]);
                int t2 = cnt.get((String)tmp[j]);
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
                    if (obj.getString("start").equals(Config.getUrl()+"/db/data/node/"+id)) flag = "ou_";
                    if (flag.equals("in_")) continue;
                    obj.put("type",flag+k);
                    rejsarr.put(obj);
                }
            }
            for (JSONObject obj : list){
                if (obj.getString("type").equals(k)) {
                    String flag = "in_";
                    if (obj.getString("start").equals(Config.getUrl()+"/db/data/node/"+id)) flag = "ou_";
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
    public static void main(String args[]){


    }
}
