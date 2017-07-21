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
        String id = request.getParameter("id");

        String p = PostUtil.sendGet("http://neo4j:1@127.0.0.1:7474/db/data/node/"+id);
        System.out.println(p);
        response.setContentType("application/json");
        response.getWriter().print(p);
    }
}
