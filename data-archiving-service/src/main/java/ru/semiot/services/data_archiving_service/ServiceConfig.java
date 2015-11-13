package ru.semiot.services.data_archiving_service;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.FIRST)
@Sources({ "file:/semiot-platform/data-archiving-service/config.properties" })
public interface ServiceConfig extends Config {

	@DefaultValue("ws://wamprouter:8080/ws")
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

	@DefaultValue("http://opentsdb:4242")
	@Key("services.tsdb.url")
	String tsdbUrl();

	@DefaultValue("http://deviceproxyservice:3030/ds/query")
	@Key("services.fuseki.store.url")
	String storeUrl();

	@DefaultValue("ru.semiot.devices.remove.sensor")
	@Key("services.topics.removeSensor")
	String topicsRemoveSensor();

	@DefaultValue("admin")
	@Key("services.fuseki.store.username")
	String storeUsername();

	@DefaultValue("pw")
	@Key("services.fuseki.store.password")
	String storePassword();
}
