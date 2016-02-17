package ru.semiot.platform.apigateway.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/UploadDriverHandler")
@MultipartConfig
public class UploadDriverHandler extends HttpServlet {

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        Part part = request.getPart("bundlefile");
        HttpClientConfig hcc = new HttpClientConfig();
        String symbolicName = hcc.sendPostUploadFile(
                BundleConstants.urlBundles, part.getInputStream(),
                part.getSubmittedFileName());

        response.sendRedirect("/config/ConfigurationDriver?symbolicName=" + symbolicName);

		// request.getRequestDispatcher("/config/ConfigurationDriver").forward(request,
        // response);
    }

}
