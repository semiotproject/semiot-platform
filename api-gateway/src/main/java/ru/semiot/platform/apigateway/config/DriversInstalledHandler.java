package ru.semiot.platform.apigateway.config;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

@WebServlet("/DriverInstalledHandler")
public class DriversInstalledHandler extends HttpServlet {

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HashMap<String, String> parameters = getRequestParameters(request);

		String symbolicName = parameters.get("id_bundle");
		String redirect = "/config/DriversInstalled";
		if (StringUtils.isNotBlank(symbolicName)) {
			if (request.getParameter("uninstall") != null) {
				try {
					QueryUtils.uninstall(symbolicName);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			} else if(request.getParameter("uninstallWithDeleteData") != null) {
				try {
					QueryUtils.uninstallWithData(symbolicName);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			} else if(request.getParameter("conf") != null) {	
				redirect = "/config/ConfigurationDriver?symbolicName="+symbolicName;
			}
		}

		response.sendRedirect(redirect);
		// request.getRequestDispatcher("/config/DriversInstalled").forward(request,
		// response);
	}

	private static HashMap<String, String> getRequestParameters(
			HttpServletRequest request) {
		HashMap<String, String> parameters = new HashMap<String, String>();
		Enumeration _enum = request.getParameterNames();
		while (_enum.hasMoreElements()) {
			String key = (String) _enum.nextElement();
			String value = request.getParameter(key);
			// request.get
			parameters.put(key, value);
			System.out.println(key + " " + value);
		}
		return parameters;
	}

}
