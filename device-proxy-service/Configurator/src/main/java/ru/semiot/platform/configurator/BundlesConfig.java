package ru.semiot.platform.configurator;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.FIRST)
@Sources({"file:/semiot-platform/device-proxy-service/config.properties"})
public interface BundlesConfig extends Config {

  @DefaultValue("http://triplestore:3030/blazegraph/sparql")
  @Key("services.triplestore.url")
  String triplestoreEndpoint();

  @DefaultValue("admin")
  @Key("services.triplestore.username")
  String triplestoreUsername();
  
  @DefaultValue("pw")
  @Key("services.triplestore.password")
  String triplestorePassword();
  
  @DefaultValue("300")
  @Key("services.deviceproxyservice.directory.store_operation_buffersize")
  String directoryStoreOperationBuffersize();
  
  @DefaultValue("1")
  @Key("services.deviceproxyservice.directory.store_operation_bufferidle")
  String directoryStoreOperationBufferidle();
  
}
