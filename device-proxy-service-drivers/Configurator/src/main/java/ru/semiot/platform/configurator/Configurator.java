package ru.semiot.platform.configurator;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.felix.webconsole.AbstractWebConsolePlugin;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 *
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
        String PID = null;
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
        //If bundle's serviceReference is null (e.g., when you only install driver without start), 
        //use loc, it setup bundle's location forcibly
        //String loc = null;
        BundleContext ctx = context;
        Bundle[] bundles = ctx.getBundles();
        for (Bundle bun : bundles) {
            if (bun.getSymbolicName().contains(PID)) {
                ctx = bun.getBundleContext();
                //loc = bun.getLocation();
                break;
            }
        }
        ServiceReference configurationAdminReference
                = ctx.getServiceReference(ConfigurationAdmin.class.getName());
        if (configurationAdminReference != null) {
            ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) ctx.getService(configurationAdminReference);
            Configuration config = (Configuration) configurationAdmin.getConfiguration(PID);
            Dictionary props = config.getProperties();
            Dictionary appProps = jsonToDictionary(json);
            if (props == null) {
                props = appProps;
            } else {
                for (Enumeration keys = appProps.keys(); keys.hasMoreElements();) {
                    Object nextElement = keys.nextElement();
                    props.put(nextElement, appProps.get(nextElement));
                }
            }
            //if(loc!=null)
            //    config.setBundleLocation(loc);
            config.update(props);
        }

    }

    private Dictionary jsonToDictionary(JSONObject obj) {
        Dictionary props = new Hashtable();
        for (Iterator<String> keys = obj.keys(); keys.hasNext();) {
            Object key = keys.next();
            props.put(key.toString(), obj.get(key.toString()).toString());
        }
        return props;
    }

    private JSONObject dictionaryToJson(Dictionary dic) {
        JSONObject json = new JSONObject();
        for (Enumeration keys = dic.keys(); keys.hasMoreElements();) {
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
