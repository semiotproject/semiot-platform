package ru.semiot.platform.apigateway;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
@WebServlet("logout")
public class Authentication extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Cache-Control", "no-cache, no-store");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Expires", new java.util.Date().toString());
        if (req.getSession(false) != null) {
            req.getSession(false).invalidate();// remove session.
        }
        req.logout();
        resp.sendRedirect("/");
    }
}
