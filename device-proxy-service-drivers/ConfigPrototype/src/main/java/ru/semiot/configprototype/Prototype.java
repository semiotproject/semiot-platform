package ru.semiot.configprototype;

import java.util.Dictionary;
import java.util.Properties;
import javax.servlet.Servlet;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Prototype implements BundleActivator {

    private ServiceRegistration pluginRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        Dictionary properties = new Properties();
        properties.put("felix.webconsole.label", "hello");
        properties.put("felix.webconsole.category", "OSGI");

        pluginRegistration = context.registerService(Servlet.class.getName(), new SimpleServlet(), properties);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.pluginRegistration != null) {
            this.pluginRegistration.unregister();
            this.pluginRegistration = null;
        }
    }

}
