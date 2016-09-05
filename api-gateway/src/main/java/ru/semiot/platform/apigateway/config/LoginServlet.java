package ru.semiot.platform.apigateway.config;

import arq.rsparql;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.http.Cookie;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
@WebServlet(urlPatterns = {"auth"})
public class LoginServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    StringBuilder builder = new StringBuilder();
    String line;
    while ((line = request.getReader().readLine()) != null) {
      builder.append(line);
    }
    try {
      JSONObject object = new JSONObject(builder.toString());
      request.login(object.optString("username"), object.optString("password"));
      response.setHeader("JSESSIONID", request.getSession().getId());      
    } catch (JSONException ex) {
      response.setStatus(400);
    } catch (ServletException ex) {
      response.setStatus(403);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (request.getHeader("Accept") != null && request.getHeader("Accept").contains("text/html")) {
      response.sendRedirect(request.getContextPath() + "/login.html");
    } else {
      response.setStatus(401);
    }
  }

}
