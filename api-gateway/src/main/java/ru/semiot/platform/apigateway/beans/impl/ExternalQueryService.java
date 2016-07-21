package ru.semiot.platform.apigateway.beans.impl;

import org.aeonbits.owner.ConfigFactory;
import org.glassfish.jersey.client.rx.Rx;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import ru.semiot.platform.apigateway.ServerConfig;
import rx.Observable;
import rx.exceptions.Exceptions;

import java.io.InputStream;
import java.io.StringReader;

import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.core.Response;

@Singleton
public class ExternalQueryService {

  private static final ServerConfig config = ConfigFactory.create(ServerConfig.class);

  private static final String urlRs = config.deviceProxyEndpoint() + "/services";
  private static final String urlRsRemoveFromFuseki = urlRs + "/remove/fuseki/";

  @javax.annotation.Resource
  ManagedExecutorService mes;

  public Observable<JsonArray> getDriversJsonArray() {
    Observable<Response> get = getForPath(config.repositoryEndpoint());

    return get.map((response) -> {
      try (JsonReader reader =
               Json.createReader(new StringReader(response.readEntity(String.class)))) {
        JsonObject jsonObject = reader.readObject();
        return jsonObject.getJsonObject("drivers").getJsonArray("driver");
      } catch (Exception e) {
        throw Exceptions.propagate(e);
      }
    });
  }

  public Observable<InputStream> getBundleInputStream(String url) {
    Observable<Response> get = getForPath(url);

    return get.map((response) -> {
      return response.readEntity(InputStream.class);
    });
  }

  public Observable<Response> sendRsRemoveFromFuseki(String pid) {
    return deleteForPath(urlRsRemoveFromFuseki + pid);
  }

  private Observable<Response> getForPath(String url) {
    return Rx.newClient(RxObservableInvoker.class, mes).target(url).request().rx().get();
  }

  private Observable<Response> deleteForPath(String url) {
    return Rx.newClient(RxObservableInvoker.class, mes).target(url).request().rx().delete();
  }

}
