package ru.semiot.platform.deviceproxyservice.launcher;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.FIRST)
@Sources({"file:/semiot-platform/config.properties"})
public interface ServiceConfig extends Config {
  
  @DefaultValue("internal")
  @Key("services.wamp.login")
  String wampLogin();
  
  @DefaultValue("internal")
  @Key("services.wamp.password")
  String wampPassword();
  
}
