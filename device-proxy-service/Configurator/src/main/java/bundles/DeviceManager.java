package bundles;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.configurator.BundlesConfig;

import java.util.Dictionary;
import java.util.Hashtable;

public class DeviceManager extends ABundle {

  private static final Logger logger = LoggerFactory.getLogger(DirectoryService.class);
  private static final BundlesConfig config = ConfigFactory.create(BundlesConfig.class);
  
  public DeviceManager() {
    super("ru.semiot.platform.deviceproxyservice.manager");
    
    Dictionary<String, String> props = new Hashtable();
    props.put("ru.semiot.platform.device-proxy-service-directory.triplestore_update_url",
        config.triplestoreEndpoint());

    props.put("ru.semiot.platform.deviceproxyservice.manager.fuseki_update_url",
        config.triplestoreEndpoint());
    props.put("ru.semiot.platform.deviceproxyservice.manager.fuseki_query_url",
        config.triplestoreEndpoint());
    props.put("ru.semiot.platform.deviceproxyservice.manager.fuseki_store_url",
        config.triplestoreEndpoint());
    
    props.put("ru.semiot.platform.deviceproxyservice.manager.fuseki_pass",
        config.triplestorePassword());
    props.put("ru.semiot.platform.deviceproxyservice.manager.fuseki_username",
        config.triplestoreUsername());
    
    props.put("ru.semiot.platform.domain",
        config.domain());

    props.put("ru.semiot.platform.deviceproxyservice.manager.wamp_uri",
        config.wampUri());
    props.put("ru.semiot.platform.wamp_login",
        config.wampLogin());
    props.put("ru.semiot.platform.wamp_password",
        config.wampPassword());
    
    this.setProperties(props);
  }
  
}
