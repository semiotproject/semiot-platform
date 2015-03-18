package ru.semiot.services.deviceproxy;

import org.aeonbits.owner.Config;

@Config.Sources({
    "file:/semiot-platform/device-proxy-service/config.properties"
})
public interface ServiceConfig extends Config {

    @DefaultValue("3131")
    @Key("services.deviceproxy.port")
    int port();

    @DefaultValue("ws://localhost/ws")
    @Key("services.deviceproxy.wamp.uri")
    String wampUri();
    
    @DefaultValue("realm1")
    @Key("services.deviceproxy.wamp.realm")
    String wampRealm();
    
    @DefaultValue("15") //seconds
    @Key("services.deviceproxy.wamp.reconnect")
    int wampReconnectInterval();
    
    @DefaultValue("ru.semiot.devices.register")
    @Key("services.topics.register")
    String topicsRegister();
}
