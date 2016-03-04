package ru.semiot.platform.apigateway.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(urlPatterns = "/UploadDriverHandler", asyncSupported = true)
@MultipartConfig
public class UploadDriverHandler extends HttpServlet {

	private static final Logger logger = 
			LoggerFactory.getLogger(UploadDriverHandler.class);
	
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

    	Part part = request.getPart("bundlefile");
    	HttpSession session = request.getSession(true);
    	
    	session.setAttribute("inputStreamFile", part.getInputStream());
    	session.setAttribute("filename", part.getSubmittedFileName());
    	
    	response.sendRedirect("/config/ConfigurationDriver");
    }

}
