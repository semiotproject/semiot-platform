package ru.semiot.platform.apigateway.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
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
        synchronized (this) {
            credentials = db.getAllUsers();
        };
        req.setAttribute("credentials", credentials);
        req.getRequestDispatcher("/configuration/AdminPanel").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String user = req.getReader().readLine();
        if (!user.contains("id") || !user.contains("login") || !user.contains("password") || !user.contains("role")) {
            resp.sendError(400);
            return;
        }
        final int id = Integer.parseInt(getParam(user, "id"));
        final String login = getParam(user, "login");
        final String pass = getParam(user, "password");
        final String role = getParam(user, "role");
        Credentials c;
        if ((c = db.addUser(id, login, pass, role)) == null) {
            resp.sendError(500);
            return;
        }
        synchronized (this) {
            credentials.add(c);
        };
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String query = req.getQueryString();
        if (!query.contains("id")) {
            resp.sendError(400);
            return;
        }
        final int id = Integer.parseInt(getParam(query, "id"));
        Credentials c;
        if ((c = findCredentialByID(id)) == null) {
            resp.sendError(404);
            return;
        }
        if (db.removeUser(id)) {
            synchronized (this) {
                credentials.remove(c);
            }
        } else {
            resp.sendError(500);
            return;
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        StringBuilder query = new StringBuilder();
        BufferedReader reader = req.getReader();
        String str;
        while ((str = reader.readLine()) != null) {
            query.append(str);
        }
        List<Credentials> changedList = parseJsonToCredentials(query.toString());
        str = null;
        Credentials c;
        for (Credentials cred : changedList) {
            if (credentials.contains(cred)) {
                if (cred.needUpdate(c = credentials.get(credentials.indexOf(cred)))) {
                    db.updateUser(cred);
                }
            } else {
                if (!cred.getLogin().isEmpty() && db.isUniqueLogin(cred.getLogin())) {
                    synchronized (this) {
                        db.addUser(cred);
                    }
                } else {
                    str = (cred.getLogin().isEmpty()) ? "Empty login!" : "Bad login: " + cred.getLogin();
                }
            }
        }
        synchronized (this) {
            this.credentials = db.getAllUsers();
        }
        if (str != null) {
            resp.sendError(400, str);
        }

    }

    private Credentials findCredentialByID(int id) {
        for (Credentials c : credentials) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    private String getParam(String query, String param) {
        final int start = query.lastIndexOf(param + "=") + (param + "=").length();
        final int end = (query.indexOf("&", start) != -1) ? query.indexOf("&", start) : query.length();
        return query.substring(start, end);
    }

    private List<Credentials> parseJsonToCredentials(String str) {
        JSONArray arr = new JSONArray(str);
        List<Credentials> lst = new ArrayList<>();
        JSONObject obj;
        for (int i = 0; i < arr.length(); i++) {
            obj = (JSONObject) arr.get(i);
            lst.add(new Credentials(obj.getInt("id"), obj.getString("login"), obj.getString("password"), obj.getString("role")));
        }
        if (lst.isEmpty()) {
            return null;
        }
        return lst;
    }
}
