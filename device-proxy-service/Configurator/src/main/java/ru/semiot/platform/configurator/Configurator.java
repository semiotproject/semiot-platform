package ru.semiot.platform.configurator;

import org.apache.felix.webconsole.AbstractWebConsolePlugin;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
public class Configurator extends AbstractWebConsolePlugin {

  public static final String TITLE = "configurator";
  public static final String LABEL = "configurator";
  BundleContext context;

  public Configurator(BundleContext bundle) {
    super();
    this.context = bundle;
  }

  @Override
  protected void renderContent(HttpServletRequest hsr, HttpServletResponse hsr1) throws ServletException, IOException {
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    String PID;
    PID = req.getParameter("pid");
    if (PID == null) {
      res.sendError(400);
      return;
    }
    StringBuilder builder = new StringBuilder();
    try (Scanner scanner = new Scanner(req.getInputStream())) {
      while (scanner.hasNextLine()) {
        builder.append(scanner.nextLine());
      }
    }
    JSONObject json = new JSONObject(builder.toString());
    if (json.length() == 0) {
      res.sendError(400);
      return;
    }
    Configuration config = getConfig(PID);
    if (config == null) {
      res.sendError(400);
      return;
    }
    Dictionary props = config.getProperties();
    Dictionary confProps = jsonToDictionary(json);
    if (props == null) {
      props = confProps;
    } else {
      for (Enumeration keys = confProps.keys(); keys.hasMoreElements(); ) {
        Object nextElement = keys.nextElement();
        props.put(nextElement, confProps.get(nextElement));
      }
    }
    //config.setBundleLocation(loc);
    config.update(props);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String PID;
    PID = request.getParameter("pid");
    if (PID == null) {
      response.sendError(404);
      return;
    }
    Configuration config = getConfig(PID);
    if (config == null) {
      response.sendError(404);
      return;
    }
    Dictionary props = config.getProperties();
    response.setContentType("application/json");
    response.setStatus(200);
    try (PrintWriter writer = response.getWriter()) {
      if (props == null) {
        writer.write("{}");
      } else {
        writer.write(dictionaryToJson(props).toString());
      }
      writer.flush();
    }
  }

  private Configuration getConfig(String pid) throws IOException {
    //If bundle's serviceReference is null (e.g., when you only install driver without start),
    //use loc, it setup bundle's location forcibly
    //String loc = null;
    BundleContext ctx = context;
    Bundle[] bundles = ctx.getBundles();
    for (Bundle bun : bundles) {
      if (bun.getSymbolicName().contains(pid)) {
        ctx = bun.getBundleContext();
        //loc = bun.getLocation();
        break;
      }
    }
    ServiceReference configurationAdminReference
        = ctx.getServiceReference(ConfigurationAdmin.class.getName());
    if (configurationAdminReference != null) {
      ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) ctx.getService(configurationAdminReference);
      Configuration config = (Configuration) configurationAdmin.getConfiguration(pid);
      return config;
    }
    return null;
  }

  private Dictionary jsonToDictionary(JSONObject obj) {
    if (obj == null) {
      return null;
    }
    Dictionary props = new Hashtable();
    for (Iterator<String> keys = obj.keys(); keys.hasNext(); ) {
      Object key = keys.next();
      props.put(key.toString(), obj.get(key.toString()).toString());
    }
    return props;
  }

  private JSONObject dictionaryToJson(Dictionary dic) {
    if (dic == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    for (Enumeration keys = dic.keys(); keys.hasMoreElements(); ) {
      Object nextElement = keys.nextElement();
      json.put(nextElement.toString(), dic.get(nextElement).toString());
    }
    return json;
  }

  @Override
  public String getLabel() {
    return LABEL;
  }

  @Override
  public String getTitle() {
    return TITLE;
  }
}
