package ru.semiot.services.data_archiving_service;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.DefaultValue;
import org.aeonbits.owner.Config.Key;

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
	
	@DefaultValue("ru.semiot.devices.newAndObserving")
    @Key("services.topics.register")
    String topicsRegister();
	
}
