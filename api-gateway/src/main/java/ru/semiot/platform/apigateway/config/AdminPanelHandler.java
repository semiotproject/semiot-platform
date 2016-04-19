package ru.semiot.platform.apigateway.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ru.semiot.platform.apigateway.utils.Credentials;
import ru.semiot.platform.apigateway.utils.DataBase;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
@WebServlet("/config/AdminPanel")
public class AdminPanelHandler extends HttpServlet {

    @Inject
    DataBase db;
    volatile List<Credentials> credentials;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String s = req.getQueryString();
        if (s!=null && s.contains("logout")) {
            resp.setHeader("Cache-Control", "no-cache, no-store");
            resp.setHeader("Pragma", "no-cache");
            resp.setHeader("Expires", new java.util.Date().toString());
            if (req.getSession(false) != null) {
                req.getSession(false).invalidate();// remove session.
            }
            req.logout();
            resp.sendRedirect("/");
        }
        synchronized (this) {
            credentials = db.getAllUsers();
        };
        req.setAttribute("credentials", credentials);
        req.getRequestDispatcher("/configuration/AdminPanel").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (req.getParameter("save") != null) {
            Map m = req.getParameterMap();
            String[] ids = (String[]) m.get("id");
            String[] logins = (String[]) m.get("login");
            String[] passwords = (String[]) m.get("password");
            String[] roles = (String[]) m.get("role");
            List<Credentials> newList = new ArrayList<>();
            Credentials c;
            for (int i = 0; i < ids.length; i++) {

                c = new Credentials(Integer.parseInt(ids[i]), logins[i], passwords[i], (i == 0) ? "admin" : roles[i]);
                if (c.getLogin().isEmpty() || c.getLogin().contains(" ") || !db.isUniqueLogin(c.getLogin(), c.getId())) {
                    if (credentials.contains(c)) {
                        newList.add(credentials.get(credentials.indexOf(c)));
                    }
                    continue;
                }
                newList.add(c);
                if (credentials.contains(c)) {
                    if (c.needUpdate(credentials.get(credentials.indexOf(c)))) {
                        if (!db.updateUser(c)) {
                            newList.remove(c);
                        }
                    }
                } else if (db.addUser(c) == null) {
                    newList.remove(c);
                }
            }
            for (Credentials q : credentials) {
                if (!newList.contains(q)) {
                    db.removeUser(q.getId());
                }
            }
            resp.sendRedirect("/config/AdminPanel");
        }
    }
}
