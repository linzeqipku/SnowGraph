package servlet;

import searcher.SnowGraphContext;
import org.json.JSONObject;
import searcher.doc.example.StackOverflowExamples;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by Administrator on 2017/5/26.
 */
public class RandomServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        String query = StackOverflowExamples.getRandomExampleQuery();
        JSONObject searchResult = new JSONObject();
        searchResult.put("query", query);

        response.getWriter().print(searchResult.toString());
    }

}