package ru.semiot.platform.configurator;

import bundles.Bundle;
import bundles.DirectoryService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.ArrayList;
import java.util.List;

public class DeviceProxyConfigurator {

  public static void configuring(BundleContext ctx) {
    ServiceReference configurationAdminReference
      = ctx.getServiceReference(ConfigurationAdmin.class.getName());
    
    if (configurationAdminReference != null) {
      ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) ctx.getService(configurationAdminReference);
      
      List<Bundle> bundles = new ArrayList<Bundle>();
      bundles.add(new DirectoryService());
      
      for(Bundle bundle : bundles) {
        bundle.configuringBundle(configurationAdmin);
      }
    }
  }
  
  
  
}
