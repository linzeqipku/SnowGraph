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
public class GetExtendsTreeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");

        String p = PostUtil.sendGet("http://neo4j:1@127.0.0.1:7474/db/data/node/"+id);
        System.out.println(p);
        JSONObject jsobj = new JSONObject(p);
        List<JSONObject> nodes = new ArrayList<>();
        List<Integer> deep = new ArrayList<>();
        List<JSONObject> relationships = new ArrayList<>();
        Set<String> nodeset = new HashSet<>();
        nodeset.add(id);
        nodes.add(jsobj);
        deep.add(0);
        Set<String> relset = new HashSet<>();
        int index = 0;
        int indexdeep = 0;
        while (true){
            if (index >= nodes.size()) break;
            JSONObject t = nodes.get(index);
            int fadeep = deep.get(index);
            if (index == 30) indexdeep = fadeep;
            if (index > 30 && fadeep > indexdeep) break;
            index++;
            id = ""+t.getJSONObject("metadata").getInt("id");
            p = PostUtil.sendGet("http://neo4j:1@127.0.0.1:7474/db/data/node/"+id+"/relationships/all");
            JSONArray jsrel = new JSONArray(p);
            Iterator<Object> it = jsrel.iterator();

            while (it.hasNext()) {

                JSONObject ob = (JSONObject) it.next();
                if (ob.get("type").equals("extend")){
                    String relid = ""+(ob.getJSONObject("metadata").getInt("id"));
                    if (!relset.contains(relid)){
                        relationships.add(ob);
                        relset.add(relid);
                        String oid = ob.getString("start");
                        oid = oid.substring(35);
                        if (!nodeset.contains(oid)){
                            String tmp = PostUtil.sendGet("http://neo4j:1@127.0.0.1:7474/db/data/node/"+oid);
                            nodes.add(new JSONObject(tmp));
                            deep.add(fadeep+1);
                            nodeset.add(oid);
                        }
                        oid = ob.getString("end");
                        oid = oid.substring(35);
                        if (!nodeset.contains(oid)){
                            String tmp = PostUtil.sendGet("http://neo4j:1@127.0.0.1:7474/db/data/node/"+oid);
                            nodes.add(new JSONObject(tmp));
                            deep.add(fadeep+1);
                            nodeset.add(oid);
                        }
                    }
                }
            }
        }
        JSONArray nodesarr = new JSONArray(nodes);
        JSONArray relsarr = new JSONArray(relationships);
        jsobj = new JSONObject();
        jsobj.put("nodes",nodesarr);
        jsobj.put("relationships",relsarr);
        //System.out.println(jsobj.toString());
        response.setContentType("application/json");
        response.getWriter().print(jsobj.toString());
    }
    public static void main(String args[]){
        String id = "5368";

        String p = PostUtil.sendGet("http://neo4j:1@127.0.0.1:7474/db/data/node/"+id);
        System.out.println(p);
        JSONObject jsobj = new JSONObject(p);
        List<JSONObject> nodes = new ArrayList<>();
        List<JSONObject> relationships = new ArrayList<>();
        Set<String> nodeset = new HashSet<>();
        nodeset.add(id);
        nodes.add(jsobj);
        Set<String> relset = new HashSet<>();
        int index = 0;
        while (true){
            if (index >= nodes.size()) break;
            JSONObject t = nodes.get(index);
            index++;
           // id = ""+t.getJSONObject("metadata").getInt("id");
            p = PostUtil.sendGet("http://neo4j:1@127.0.0.1:7474/db/data/node/"+id+"/relationships/all");
            JSONArray jsrel = new JSONArray(p);
            Iterator<Object> it = jsrel.iterator();
            while (it.hasNext()) {
                JSONObject ob = (JSONObject) it.next();
                if (ob.getString("type").equals("implement") || ob.get("type").equals("extend")){
                    String relid = ""+(ob.getJSONObject("metadata").getInt("id"));
                    if (!relset.contains(relid)){
                        relationships.add(ob);
                        relset.add(relid);
                        String oid = ob.getString("start");
                        oid = oid.substring(35);
                        if (!nodeset.contains(oid)){
                            String tmp = PostUtil.sendGet("http://neo4j:1@127.0.0.1:7474/db/data/node/"+oid);
                            nodes.add(new JSONObject(tmp));
                         //   nodeset.add(oid);
                        }
                        oid = ob.getString("end");
                        oid = oid.substring(35);
                        if (!nodeset.contains(oid)){
                            String tmp = PostUtil.sendGet("http://neo4j:1@127.0.0.1:7474/db/data/node/"+oid);
                            nodes.add(new JSONObject(tmp));
                           // nodeset.add(oid);
                        }
                    }
                }
            }
        }
        JSONArray nodesarr = new JSONArray(nodes);
        JSONArray relsarr = new JSONArray(relationships);
        jsobj = new JSONObject();
        jsobj.put("nodes",nodesarr);
        jsobj.put("relationships",relsarr);
        System.out.println(jsobj.toString());
    }
}
