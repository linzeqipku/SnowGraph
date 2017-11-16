package servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
    	
    	response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    	
        String id = request.getParameter("id");
        try (Statement statement = Config.getNeo4jBoltConnection().createStatement()) {
        	String stat="match (n) where id(n)="+id+" return n";
        	ResultSet rs=statement.executeQuery(stat);
        	while (rs.next())
        		response.getWriter().print(rs.getString("n"));
        } catch (SQLException e){
        	e.printStackTrace();
        }
    }
    
}
