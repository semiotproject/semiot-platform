package ru.semiot.platform.apigateway.config;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@WebServlet(urlPatterns = "/UploadDriverHandler", asyncSupported = true)
@MultipartConfig
public class UploadDriverHandler extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(UploadDriverHandler.class);

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

    	Part part = request.getPart("bundlefile");
    	HttpSession session = request.getSession(true);

		for(Part p : request.getParts()) {
			logger.debug("Part name: {}", p.getName());
			logger.debug("Part [{}] fileName: {}", p.getName(),
					p.getSubmittedFileName());
			logger.debug("Part [{}] size: {}", p.getName(), p.getSize());
			logger.debug("Part [{}] inputstream: {}", p.getName(),
					IOUtils.toString(p.getInputStream()));
		}
    	
    	session.setAttribute("inputStreamFile", part.getInputStream());
    	session.setAttribute("filename", part.getSubmittedFileName());
    	
    	response.sendRedirect("/config/ConfigurationDriver");
    }

}
