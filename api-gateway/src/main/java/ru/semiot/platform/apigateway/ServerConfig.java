package ru.semiot.platform.apigateway;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.FIRST)
@Sources({"file:/semiot-platform/config.properties"})
public interface ServerConfig extends Config {

  @DefaultValue("http://triplestore:3030/blazegraph/sparql")
  @Key("services.triplestore.url")
  String sparqlEndpoint();

  @DefaultValue("admin")
  @Key("services.triplestore.username")
  String sparqlUsername();

  @DefaultValue("pw")
  @Key("services.triplestore.password")
  String sparqlPassword();

  @DefaultValue("ws://wamprouter:8080/ws")
  @Key("services.wamp.uri")
  String wampUri();

  @DefaultValue("realm1")
  @Key("services.wamp.realm")
  String wampRealm();

  @DefaultValue("/wamp")
  @Key("services.wamp.public.path")
  String wampPublicPath();

  @DefaultValue("wss")
  @Key("services.wamp.protocol.scheme")
  String wampProtocolScheme();

  @Key("services.dataarchiving.url")
  @DefaultValue("http://dataarchivingservice:8787")
  String tsdbEndpoint();

  @Key("services.dataarchiving.commandResults.path")
  @DefaultValue("/commandResults")
  String tsdbCommandResultsPath();

  @Key("services.dataarchiving.commandResults.latest.path")
  @DefaultValue("/commandResults/latest")
  String tsdbCommandResultsLatestPath();

  @DefaultValue("http://deviceproxyservice:8181")
  @Key("services.deviceproxy.url")
  String deviceProxyEndpoint();

  @Key("services.deviceproxy.commands.path")
  @DefaultValue("/services/commands")
  String deviceProxyCommandAPI();

  @DefaultValue(
      "https://raw.githubusercontent.com/semiotproject/semiot-platform/bundles/drivers.json")
  @Key("services.repository.url")
  String repositoryEndpoint();

  @DefaultValue("10")
  @Key("apigateway.systems.pagesize")
  int systemsPageSize();

}
