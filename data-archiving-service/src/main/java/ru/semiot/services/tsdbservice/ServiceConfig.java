package ru.semiot.services.tsdbservice;

import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;
import org.apache.jena.riot.RDFLanguages;

@LoadPolicy(LoadType.FIRST)
@Sources({"file:/semiot-platform/config.properties"})
public interface ServiceConfig extends Mutable {

  public static final ServiceConfig CONFIG = ConfigFactory.create(ServiceConfig.class);

  @DefaultValue("ws://wamprouter:8080/ws")
  @Key("services.wamp.uri")
  String wampUri();

  @DefaultValue("realm1")
  @Key("services.wamp.realm")
  String wampRealm();

  @DefaultValue("internal")
  @Key("services.wamp.login")
  String wampLogin();

  @DefaultValue("internal")
  @Key("services.wamp.password")
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

  @DefaultValue("http://triplestore:3030/blazegraph/sparql")
  @Key("services.triplestore.url")
  String storeUrl();

  @DefaultValue("ru.semiot.devices.remove.sensor")
  @Key("services.topics.removeSensor")
  String topicsRemoveSensor();

  @DefaultValue("admin")
  @Key("services.triplestore.username")
  String storeUsername();

  @DefaultValue("pw")
  @Key("services.triplestore.password")
  String storePassword();

  @DefaultValue("http://localhost/")
  @Key("semiot.platform.domain")
  String rootUrl();
}
