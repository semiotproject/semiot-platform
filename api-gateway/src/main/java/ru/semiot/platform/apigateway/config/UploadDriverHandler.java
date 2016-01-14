package ru.semiot.platform.apigateway.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.lang3.StringUtils;

@WebServlet("/UploadDriverHandler")
@MultipartConfig
public class UploadDriverHandler extends HttpServlet {
	private static String urlBundles = "http://localhost:8181/system/console/bundles";
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	Part part = request.getPart("bundlefile");
    	HttpClientConfig hcc = new HttpClientConfig();
    	String symbolicName = hcc.sendPostUploadFile(urlBundles, part.getInputStream(), part.getSubmittedFileName());
		if(StringUtils.isNotBlank(symbolicName)) {
    		request.setAttribute("symbolicName", symbolicName);
    	}
    	request.getRequestDispatcher("/config/ConfigurationDriver").forward(request, response);
    }
    
}
