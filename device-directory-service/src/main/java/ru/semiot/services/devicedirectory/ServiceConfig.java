package ru.semiot.services.devicedirectory;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.FIRST)
@Sources({ "file:/semiot-platform/device-directory-service/config.properties" })
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

	@DefaultValue("ru.semiot.devices.register")
	@Key("services.topics.register")
	String topicsRegister();

	@DefaultValue("ru.semiot.devices.new")
	@Key("services.topics.newdevice")
	String topicsNewDevice();

	@DefaultValue("ru.semiot.devices.turnoff")
	@Key("services.topics.inactive")
	String topicsInactive();

	@DefaultValue("ru.semiot.devices.remove.device")
	@Key("services.topics.removeDevice")
	String topicsRemoveDevice();

	@DefaultValue("ru.semiot.devices.remove.sensor")
	@Key("services.topics.removeSensor")
	String topicsRemoveSensor();

	@DefaultValue("http://localhost:3030/ds/data")
	@Key("services.devicedirectory.store.url")
	String storeUrl();

	@DefaultValue("http://localhost:3030/ds/query")
	@Key("services.devicedirectory.query.url")
	String queryUrl();

	@DefaultValue("http://localhost:3030/ds/update")
	@Key("services.devicedirectory.update.url")
	String updateUrl();

	@DefaultValue("admin")
	@Key("services.devicedirectory.store.username")
	String storeUsername();

	@DefaultValue("pw")
	@Key("services.devicedirectory.store.password")
	String storePassword();
}
