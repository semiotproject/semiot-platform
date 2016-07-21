package bundles;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;

public abstract class ABundle {

  private static final Logger logger = LoggerFactory.getLogger(ABundle.class);

  private String pid;
  private Dictionary properties;

  ABundle(String pid) {
    this.pid = pid;
  }
  
  public void setProperties(Dictionary properties) {
    this.properties = properties;
  }

  public void configuringBundle(BundleContext ctx, HashMap<String, BundleContext> contextBundles) {
    if (pid != null && properties != null) {
      if(contextBundles.containsKey(pid)) {
        ctx = contextBundles.get(pid);
      }
      ServiceReference configurationAdminReference =
          ctx.getServiceReference(ConfigurationAdmin.class.getName());

      if (configurationAdminReference != null) {
        ConfigurationAdmin configurationAdmin =
            (ConfigurationAdmin) ctx.getService(configurationAdminReference);
        try {
          Configuration config = configurationAdmin.getConfiguration(pid);
          config.update(properties);
        } catch (IOException e) {
          logger.error(e.getMessage(), e);
        }
      }
    }
  }

}

