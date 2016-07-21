package bundles;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.configurator.BundlesConfig;

import java.util.Dictionary;
import java.util.Hashtable;

public class DirectoryService extends ABundle {

  private static final Logger logger = LoggerFactory.getLogger(DirectoryService.class);
  private static final BundlesConfig config = ConfigFactory.create(BundlesConfig.class);
  
  public DirectoryService() {
    super("ru.semiot.platform.device-proxy-service-directory");
    
    Dictionary<String, String> props = new Hashtable();
    props.put("ru.semiot.platform.device-proxy-service-directory.triplestore_update_url",
        config.triplestoreEndpoint());
    props.put("ru.semiot.platform.device-proxy-service-directory.triplestore_query_url",
        config.triplestoreEndpoint());
    props.put("ru.semiot.platform.device-proxy-service-directory.triplestore_store_url",
        config.triplestoreEndpoint());
    
    props.put("ru.semiot.platform.device-proxy-service-directory.triplestore_pass",
        config.triplestorePassword());
    props.put("ru.semiot.platform.device-proxy-service-directory.triplestore_username",
        config.triplestoreUsername());
    
    props.put("ru.semiot.platform.device-proxy-service-directory.store_operation_buffersize",
        config.directoryStoreOperationBuffersize());
    props.put("ru.semiot.platform.device-proxy-service-directory.store_operation_bufferidle",
        config.directoryStoreOperationBufferidle());
    
    this.setProperties(props);
  }
  
}
