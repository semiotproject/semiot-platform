package ru.semiot.platform.apigateway.config;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.apigateway.beans.TSDBQueryService;
import ru.semiot.platform.apigateway.beans.impl.OSGiApiService;
import ru.semiot.platform.apigateway.utils.DataBase;
import rx.Observable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(urlPatterns = "/config/ConfigurationDriver", asyncSupported = true)
public class ConfigurationDriverHandler extends HttpServlet {

  private static final Logger logger = LoggerFactory
      .getLogger(ConfigurationDriverHandler.class);

  static final String nameConfigFile = "config-schema.jsonld";

  @Inject
  private OSGiApiService service;

  @Inject
  private TSDBQueryService tsdb;

  @Inject
  private DataBase db;

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    final AsyncContext ctx = request.startAsync();

    HttpSession session = request.getSession(true);
    InputStream is = (InputStream) session.getAttribute("inputStreamFile");
    session.removeAttribute("inputStreamFile");

    byte[] byteArray = IOUtils.toByteArray(is);
    InputStream input1 = new ByteArrayInputStream(byteArray);
    InputStream input2 = new ByteArrayInputStream(byteArray);
    ZipInputStream zis = new ZipInputStream(input1);
    ZipEntry ze = zis.getNextEntry();
    StringBuilder s = new StringBuilder();
    byte[] buffer = new byte[1024];
    int read = 0;
    while (ze != null) {
      String name = ze.getName();
      if (name.contains(nameConfigFile)) {
        while ((read = zis.read(buffer, 0, 1024)) >= 0) {
          s.append(new String(buffer, 0, read));
        }
        break;
      }
      ze = zis.getNextEntry();
    }

    JsonObject jsonConfig = null;
    try (JsonReader reader = Json
        .createReader(new StringReader(s.toString()))) {
      jsonConfig = reader.readObject();
    } catch (Exception e) {
      e.printStackTrace();
    }
    session.setAttribute("inputStreamFile", input2);
    session.setAttribute("jsonConfig", jsonConfig);

    ctx.dispatch("/configuration/ConfigurationDriver");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HashMap<String, String> parameters = getRequestParameters(request);

    if (request.getParameter("save") != null
        || request.getParameter("configure") != null) {
      StringBuilder propertyList = new StringBuilder();
      if (parameters.size() < 1) {
        return;
      }
      for (Entry<String, String> entry : parameters.entrySet()) {
        if (propertyList.length() > 0) {
          propertyList.append(",");
        }
        propertyList.append(entry.getKey());
      }
      parameters.put("apply", "true");
      parameters.put("propertylist", propertyList.toString());

      final AsyncContext ctx = request.startAsync();
      HttpSession session = request.getSession(true);
      String pid = (String) session.getAttribute("pid");
      session.removeAttribute("pid");
      Observable<String> post = null;
      if (request.getParameter("save") != null) {
        parameters.remove("save");
        // ЗАГРУЗКА!!!!!!!!!!!!!
        InputStream is = (InputStream) session.getAttribute("inputStreamFile");
        String filename = (String) session.getAttribute("filename");
        session.removeAttribute("inputStreamFile");
        session.removeAttribute("filename");
        post = service.sendPostUploadFile(is, filename, pid, parameters);
      } else if (request.getParameter("configure") != null) {
        parameters.put("propertylist",
            parameters.get("propertylist")
                .concat(",")
                .concat(BundleConstants.WAMP_LOGIN)
                .concat(",")
                .concat(BundleConstants.WAMP_PASSWORD));
        String domain = parameters.get(BundleConstants.MANAGER_DOMAIN);
        String role = "internal";
        String login = RandomStringUtils.randomAlphanumeric(12);
        String pass = RandomStringUtils.randomAlphanumeric(12);
        db.addUser(0, login, pass, role);

        JsonObject json = Json.createObjectBuilder()
            .add(BundleConstants.MANAGER_DOMAIN, domain)
            .add(BundleConstants.WAMP_LOGIN, login)
            .add(BundleConstants.WAMP_PASSWORD, pass)
            .build();

        tsdb.submitConfiguration(json).subscribe();

        parameters.put(BundleConstants.WAMP_LOGIN, login);
        parameters.put(BundleConstants.WAMP_PASSWORD, pass);
        post = service.sendPostConfigStart(pid, parameters);
      }

      post.subscribe((symbolicName) -> {},
          (Throwable e) -> logger.error(e.getMessage(), e),
          () -> {
            try {
              // response.flushBuffer();
              response.sendRedirect("/config/DriversInstalled");
            } catch (Exception e1) {
              logger.error(e1.getMessage(), e1);
            }
            ctx.complete();
          });
    } else {
      response.sendRedirect("/config/DriversInstalled");
    }
  }

  private static HashMap<String, String> getRequestParameters(
      HttpServletRequest request) {
    HashMap<String, String> parameters = new HashMap<>();
    Enumeration _enum = request.getParameterNames();
    while (_enum.hasMoreElements()) {
      String key = (String) _enum.nextElement();
      String value = request.getParameter(key);
      parameters.put(key, value);
    }
    return parameters;
  }

}
