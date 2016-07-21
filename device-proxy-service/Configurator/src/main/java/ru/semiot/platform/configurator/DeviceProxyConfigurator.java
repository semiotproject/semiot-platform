package ru.semiot.platform.configurator;

import bundles.ABundle;
import bundles.DirectoryService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceProxyConfigurator {

  public static void configuring(BundleContext ctx) {
    
    HashMap<String, BundleContext> contextBundles = new HashMap<String, BundleContext>();
    Bundle[] bundles = ctx.getBundles();
    for (Bundle bun : bundles) {
      contextBundles.put(bun.getSymbolicName(), bun.getBundleContext());
    }
    
    List<ABundle> confBundles = new ArrayList<ABundle>();
    confBundles.add(new DirectoryService());
    
    for(ABundle bundle : confBundles) {
      bundle.configuringBundle(ctx, contextBundles);
    }
    
    
  }
  
  
  
}
