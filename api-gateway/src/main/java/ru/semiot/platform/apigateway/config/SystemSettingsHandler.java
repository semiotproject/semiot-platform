package ru.semiot.platform.apigateway.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/SystemSettingsHandler")
public class SystemSettingsHandler extends HttpServlet {
	private static String url = "http://localhost:8181/system/console/configMgr/ru.semiot.platform.deviceproxyservice.manager";
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	HashMap<String, String> parameters = getRequestParameters(request);
    	
    	if (request.getParameter("save") != null) {
    		HttpClientConfig hcc = new HttpClientConfig();
    		try {
    			parameters.remove("save");
    			StringBuilder propertyList = new StringBuilder();
    			if (parameters.size() < 1) {
    				return;
    			}
    			for(Entry<String, String> entry : parameters.entrySet()) {
    				if(propertyList.length() > 0) {
    					propertyList.append(",");
    				}
    				propertyList.append(entry.getKey());
    			}
    			System.out.println(propertyList.toString());
    			parameters.put("post", "true");
    			parameters.put("apply", "true");
    			parameters.put("propertylist", propertyList.toString());
    			
				hcc.sendGetUrl(url, parameters, true);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
        }
    	
    	
    	request.getRequestDispatcher("/config/DriversInstalled").forward(request, response);
    }
    
    private static HashMap<String, String> getRequestParameters(HttpServletRequest request) {
        HashMap<String, String> parameters = new HashMap<String, String>();
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
