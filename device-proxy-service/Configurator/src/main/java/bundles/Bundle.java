package bundles;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;

public abstract class Bundle {

  private static final Logger logger = LoggerFactory.getLogger(Bundle.class);

  private String pid;
  private Dictionary properties;

  Bundle(String pid) {
    this.pid = pid;
  }
  
  public void setProperties(Dictionary properties) {
    this.properties = properties;
  }

  public void configuringBundle(ConfigurationAdmin configurationAdmin) {
    if (pid != null && properties != null) {
      try {
        Configuration config = configurationAdmin.getConfiguration(pid);
        config.update(properties);
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
      }
    }
  }

}

