package ru.semiot.platform.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.apigateway.beans.impl.ExternalQueryService;
import ru.semiot.platform.apigateway.beans.impl.OSGiApiService;
import rx.Observable;
import rx.exceptions.Exceptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(urlPatterns = "/config/AvailableDrivers", asyncSupported = true)
public class AvailableDriversHandler extends HttpServlet {

  private static final Logger logger = LoggerFactory
      .getLogger(AvailableDriversHandler.class);

  @Inject
  OSGiApiService service;
  @Inject
  ExternalQueryService extService;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    final AsyncContext ctx = req.startAsync();

    Observable<JsonArray> jsonBundles = extService.getDriversJsonArray();
    Observable<List<String>> listInstalledBundles = service
        .getPidListBundles();

    Observable.zip(jsonBundles, listInstalledBundles,
        (jnBundles, lstInstalledBundles) -> {

          ctx.getRequest().setAttribute("jnBundles", jnBundles);
          ctx.getRequest().setAttribute("lstInstalledBundles",
              lstInstalledBundles);

          return new String();
        }).subscribe(ConfigHelper.dispatch(ctx,
        "/configuration/AvailableDrivers"));
  }

  protected void doPost(HttpServletRequest request,
                        HttpServletResponse response) throws ServletException, IOException {
    HashMap<String, String> parameters = getRequestParameters(request);

    if (request.getParameter("install") != null) {
      final AsyncContext ctx = request.startAsync();

      String url = parameters.get("url");
      Observable<InputStream> bundleIS = extService
          .getBundleInputStream(url);

      bundleIS.map((inputStream) -> {
        HttpSession session = request.getSession(true);
        session.setAttribute("inputStreamFile", inputStream); // ctx.getRequest()
        session.setAttribute("filename", url);
        try {
          response.sendRedirect("/config/ConfigurationDriver");
        } catch (Exception e1) {
          throw Exceptions.propagate(e1);
        }
        return new String();
      }).subscribe((__) -> {},
          (Throwable e) -> logger.error(e.getMessage(), e),
          () -> ctx.complete());
    }
  }

  private static HashMap<String, String> getRequestParameters(
      HttpServletRequest request) {
    HashMap<String, String> parameters = new HashMap<String, String>();
    Enumeration _enum = request.getParameterNames();
    while (_enum.hasMoreElements()) {
      String key = (String) _enum.nextElement();
      String value = request.getParameter(key);
      parameters.put(key, value);
    }
    return parameters;
  }

}
