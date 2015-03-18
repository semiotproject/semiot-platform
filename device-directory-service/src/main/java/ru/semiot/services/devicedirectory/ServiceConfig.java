package ru.semiot.services.devicedirectory;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.FIRST)
@Sources({
    "file:/semiot-platform/device-directory-service/config.properties"
})
public interface ServiceConfig extends Config {

    @DefaultValue("ws://localhost/ws")
    @Key("services.wamp.uri")
    String wampUri();

    @DefaultValue("realm1")
    @Key("services.wamp.realm")
    String wampRealm();

    @DefaultValue("15") //seconds
    @Key("services.wamp.reconnect")
    int wampReconnectInterval();

    @DefaultValue("ru.semiot.devices.register")
    @Key("services.topics.register")
    String topicsRegister();
    
    @DefaultValue("http://localhost:3030/ds/update")
    @Key("services.devicedirectory.store.url")
    String storeUrl();
    
    @DefaultValue("")
    @Key("services.devicedirectory.store.username")
    String storeUsername();
    
    @DefaultValue("")
    @Key("services.devicedirectory.store.password")
    String storePassword();
}
