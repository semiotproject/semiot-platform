package ru.semiot.platform.configurator;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Properties;

import javax.servlet.Servlet; 

public class Activator implements BundleActivator {

  private static final Logger logger = LoggerFactory.getLogger(Activator.class);
  
  private ServiceRegistration pluginRegistration;

  @Override
  public void start(BundleContext context) throws Exception {
    Dictionary properties = new Properties();
    properties.put("felix.webconsole.label", WebConfigurator.LABEL);
    pluginRegistration = context.registerService(Servlet.class.getName(), new WebConfigurator(context), properties);
    
    DeviceProxyConfigurator.configuring(context);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    if (this.pluginRegistration != null) {
      this.pluginRegistration.unregister();
      this.pluginRegistration = null;
    }
  }
  
  
}
