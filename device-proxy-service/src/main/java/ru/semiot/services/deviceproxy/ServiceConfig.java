package ru.semiot.services.deviceproxy;

import org.aeonbits.owner.Config;

@Config.LoadPolicy(Config.LoadType.FIRST)
@Config.Sources({ "file:/semiot-platform/device-proxy-service/config.properties" })
public interface ServiceConfig extends Config {

	@DefaultValue("3131")
	@Key("services.deviceproxy.port")
	int port();

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

	@DefaultValue("TURTLE")
	@Key("services.wamp.message.format")
	String wampMessageFormat();

	@DefaultValue("ru.semiot.devices.register")
	@Key("services.topics.register")
	String topicsRegister();

	@DefaultValue("ru.semiot.devices.new")
	@Key("services.topics.newdevice")
	String topicsNewDevice();

	@DefaultValue("ru.semiot.devices.newandobserving")
	@Key("services.topics.newandobserving")
	String topicsNewAndObserving();

	@DefaultValue("ru.semiot.devices.remove")
	@Key("services.topics.remove")
	String topicsRemove();

	@DefaultValue("ru.semiot.devices.removeDevice")
	@Key("services.topics.removeDevice")
	String topicsRemoveDevice();

	@DefaultValue("ru.semiot.devices.turnoff")
	@Key("services.topics.inactive")
	String topicsInactive();

	@DefaultValue("${services.wamp.uri}?topic=%s")
	@Key("services.mappingToWAMP")
	String mappingToWAMP(final String topic);

	@DefaultValue("@prefix saref: <http://ontology.tno.nl/saref#>. <%s> saref:hasState saref:OffState.")
	@Key("services.mappingToOffState")
	String mappingToOffState(final String wampTopic);

	@DefaultValue("@prefix saref: <http://ontology.tno.nl/saref#>. <%s> saref:hasState saref:OnState.")
	@Key("services.mappingToOnState")
	String mappingToOnState(final String wampTopic);
}
