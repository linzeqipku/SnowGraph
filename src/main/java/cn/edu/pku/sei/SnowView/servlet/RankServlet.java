package cn.edu.pku.sei.SnowView.servlet;

import org.json.JSONObject;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2017/5/26.
 */
public class RankServlet extends HttpServlet {

	
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        String result = readString2();
        response.getWriter().print(result.toString());
    }
    private static String readString2()

    {

        StringBuffer str=new StringBuffer("");

        File file=new File("D:\\IdeaProjects\\SnowGraph\\src\\main\\java\\cn\\edu\\pku\\sei\\SnowView\\b.txt");

        try {

            FileReader fr=new FileReader(file);

            int ch = 0;

            while((ch = fr.read())!=-1 )

            {

                str.append((char)ch);
            }

            fr.close();

        } catch (IOException e) {

            // TODO Auto-generated catch block

            e.printStackTrace();

            System.out.println("File reader出错");

        }

        return str.toString();

    }
}