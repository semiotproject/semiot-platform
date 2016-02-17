package ru.semiot.platform.apigateway.config;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ConfigurationDriverHandler")
public class ConfigurationDriverHandler extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        HashMap<String, String> parameters = getRequestParameters(request);
        String pid = parameters.get("pid");

        if (request.getParameter("save") != null) {
            try {
                String statusConf = QueryUtils.getStatusConfigurations();
                if (statusConf.indexOf("\"PID = " + pid + "\"") == -1) {

                    parameters.remove("pid");
                    parameters.remove("save");

                    // parameters
                    StringBuilder propertyList = new StringBuilder();
                    if (parameters.size() < 1) {
                        return;
                    }
                    for (Entry<String, String> entry : parameters.entrySet()) {
                        if (propertyList.length() > 0) {
                            propertyList.append(",");
                        }
                        propertyList.append(entry.getKey());
                    }
                    System.out.println(propertyList.toString());
                    parameters.put("post", "true");
                    parameters.put("apply", "true");
                    parameters.put("propertylist", propertyList.toString());

                    QueryUtils.sendGetUrl(BundleConstants.urlConfigMgr + pid,
                            parameters, true);

                    QueryUtils.start(pid);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (request.getParameter("uninstall") != null) {
            try {
                QueryUtils.uninstall(pid);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        response.sendRedirect("/config/DriversInstalled");
		// request.getRequestDispatcher("/config/DriversInstalled").forward(request,
        // response);
    }

    private static HashMap<String, String> getRequestParameters(
            HttpServletRequest request) {
        HashMap<String, String> parameters = new HashMap<>();
        Enumeration _enum = request.getParameterNames();
        while (_enum.hasMoreElements()) {
            String key = (String) _enum.nextElement();
            String value = request.getParameter(key);
            parameters.put(key, value);
            System.out.println(key + " " + value);
        }
        return parameters;
    }

}
