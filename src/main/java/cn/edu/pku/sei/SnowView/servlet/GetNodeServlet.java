package cn.edu.pku.sei.SnowView.servlet;
import cn.edu.pku.sei.SnowView.utils.PostUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Administrator on 2017/5/26.
 */
public class GetNodeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	if (Config.sendToSlaveUrl(request,response,"GetNode")==1)
    		return;
    	
        String id = request.getParameter("id");
        //System.out.println("GetNode: "+id);

        String p = PostUtil.sendGet(Config.getUrl()+"/db/data/node/"+id);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(p);
    }
    
    public void destroy() {
    	Config.getGraphDB().shutdown();
    }
}
