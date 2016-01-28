package ru.semiot.platform.apigateway.config;

import java.io.File;
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
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

@WebServlet("/AvalaibleDriversHandler")
public class AvalaibleDriversHandler extends HttpServlet {

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HashMap<String, String> parameters = getRequestParameters(request);

		if (request.getParameter("install") != null) {
			HttpClientConfig hcc = new HttpClientConfig();
			String symbolicName = hcc.sendUploadAvalaibleFile(
					parameters.get("url"), BundleConstants.urlBundles);
			if (StringUtils.isNotBlank(symbolicName)) {
				HttpSession session = request.getSession(false);
				session.setAttribute("symbolicName", symbolicName);
			}
		}

		response.sendRedirect("/config/ConfigurationDriver");

		// request.getRequestDispatcher("/config/ConfigurationDriver").forward(request,
		// response);
	}

	private static HashMap<String, String> getRequestParameters(
			HttpServletRequest request) {
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
