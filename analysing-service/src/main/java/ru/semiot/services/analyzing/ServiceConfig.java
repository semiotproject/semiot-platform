package ru.semiot.services.analyzing;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.ConfigFactory;

@LoadPolicy(LoadType.FIRST)
@Sources({"classpath:config.properties"})
public interface ServiceConfig extends Config {

    public static final ServiceConfig config = ConfigFactory
            .create(ServiceConfig.class);

    @DefaultValue("ws://localhost/ws")
    @Key("services.wamp.uri")
    String wampUri();

    @DefaultValue("realm1")
    @Key("services.wamp.realm")
    String wampRealm();

    @DefaultValue("15")
    // seconds
    @Key("services.wamp.reconnect")
    int wampReconnectInterval();

    @DefaultValue("ru.semiot.devices.newandobserving")
    @Key("services.topics.subscriber")
    String topicsSubscriber();

    @DefaultValue("http://localhost:3030/ds/query")
    @Key("services.devicedirectory.store.url")
    String storeUrl();

    @DefaultValue("")
    @Key("services.devicedirectory.store.username")
    String storeUsername();

    @DefaultValue("")
    @Key("services.devicedirectory.store.password")
    String storePassword();

    @DefaultValue("ru.semiot.alerts")
    @Key("services.analyzingservice.alerts")
    String topicsAlert();
    
    @DefaultValue("false")
    @Key("services.analyzingservice.autoloaded")
    boolean isAutoLoaded();
}
