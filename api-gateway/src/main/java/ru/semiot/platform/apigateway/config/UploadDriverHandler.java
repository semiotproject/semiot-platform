package ru.semiot.platform.apigateway.config;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = "/UploadDriverHandler", asyncSupported = true)
@MultipartConfig
public class UploadDriverHandler extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(UploadDriverHandler.class);
    private static final String ATTR_INPUSTREAM = "inputStreamFile";
    private static final String ATTR_FILENAME = "filename";
    private static final String PART_NAME = "bundlefile";

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        Part part = request.getPart(PART_NAME);

        if (part != null && part.getInputStream() != null && part.getSize() > 0) {
            HttpSession session = request.getSession(true);

            session.setAttribute(ATTR_INPUSTREAM, part.getInputStream());
            session.setAttribute(ATTR_FILENAME, part.getSubmittedFileName());

            response.sendRedirect("/config/ConfigurationDriver");
        } else {
            final AsyncContext ctx = request.startAsync();
            if (logger.isDebugEnabled()) {
                for (Part p : request.getParts()) {
                    logger.debug("Part name: {}", p.getName());
                    logger.debug("Part [{}] fileName: {}", p.getName(),
                            p.getSubmittedFileName());
                    logger.debug("Part [{}] size: {}", p.getName(), p.getSize());

                    String partOfIS = p.getInputStream() == null ? "null" :
                            new String(IOUtils.toByteArray(p.getInputStream(), 100));
                    logger.debug("Part [{}] inputstream (first 100 bytes): {}",
                            p.getName(), partOfIS);
                }
            }

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            ctx.complete();
        }
    }

}
