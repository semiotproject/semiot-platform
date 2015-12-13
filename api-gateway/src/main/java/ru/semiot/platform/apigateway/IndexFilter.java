package ru.semiot.platform.apigateway;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import ru.semiot.platform.apigateway.utils.URIUtils;

@WebFilter(urlPatterns = {"/*"}, asyncSupported = true)
public class IndexFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final String apiURL = URIUtils.rewriteURI(request, "/doc");

        HttpServletResponse hsr = (HttpServletResponse) response;

        hsr.addHeader("Link", "<" + apiURL + ">; rel=\"http://www.w3.org/ns/hydra/core#apiDocumentation\"");

        chain.doFilter(request, hsr);
    }

    @Override
    public void destroy() {

    }
}
