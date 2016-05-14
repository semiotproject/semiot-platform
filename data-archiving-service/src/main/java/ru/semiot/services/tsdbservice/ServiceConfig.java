package ru.semiot.services.tsdbservice;

import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;
import org.apache.jena.riot.RDFLanguages;

@LoadPolicy(LoadType.FIRST)
@Sources({"file:/semiot-platform/data-archiving-service/config.properties"})
public interface ServiceConfig extends Mutable {

  public static final ServiceConfig CONFIG = ConfigFactory.create(ServiceConfig.class);

  @DefaultValue("ws://wamprouter:8080/ws")
  @Key("services.wamp.uri")
  String wampUri();

  @DefaultValue("realm1")
  @Key("services.wamp.realm")
  String wampRealm();

  @DefaultValue("internal")
  @Key("ru.semiot.platform.wamp_login")
  String wampLogin();

  @DefaultValue("internal")
  @Key("ru.semiot.platform.wamp_password")
  String wampPassword();

  @DefaultValue("15")
  // seconds
  @Key("services.wamp.reconnect")
  int wampReconnectInterval();

  @Key("services.wamp.messageFormat")
  @DefaultValue(RDFLanguages.strLangJSONLD)
  String wampMessageFormat();

  @DefaultValue("ru.semiot.devices.newandobserving")
  @Key("services.topics.subscriber")
  String topicsSubscriber();

  @DefaultValue("tsdb")
  @Key("services.tsdb.url")
  String tsdbUrl();

  @DefaultValue("http://fuseki:3030/ds/query")
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

  @DefaultValue("http://localhost/")
  @Key("ru.semiot.platform.domain")
  String rootUrl();
}
