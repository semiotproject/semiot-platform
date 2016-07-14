package ru.semiot.platform.configurator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;
import java.util.Properties;

import javax.servlet.Servlet;

public class Activator implements BundleActivator {

  private ServiceRegistration pluginRegistration;

  @Override
  public void start(BundleContext context) throws Exception {
    Dictionary properties = new Properties();
    properties.put("felix.webconsole.label", Configurator.LABEL);
    pluginRegistration = context.registerService(Servlet.class.getName(), new Configurator(context), properties);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    if (this.pluginRegistration != null) {
      this.pluginRegistration.unregister();
      this.pluginRegistration = null;
    }
  }

}
