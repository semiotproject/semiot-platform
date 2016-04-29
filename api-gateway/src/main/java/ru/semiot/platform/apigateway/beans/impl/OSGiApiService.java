package ru.semiot.platform.apigateway.beans.impl;

import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.util.Collections;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.glassfish.jersey.client.rx.Rx;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.apigateway.ServerConfig;
import ru.semiot.platform.apigateway.config.BundleConstants;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Singleton
public class OSGiApiService {

  private static final Logger logger = LoggerFactory
      .getLogger(OSGiApiService.class);
  private static final ServerConfig config = ConfigFactory
      .create(ServerConfig.class);
  private static final String URL_BUNDLES_JSON = "/system/console/bundles.json";
  private static final String URL_STATUS_CONFIGURATIONS_JSON =
      "/system/console/status-Configurations.json";
  private static final String URL_CONFIG_MGR = "/system/console/configMgr/";
  private static final String URL_SERVICES_JSON = "/system/console/services.json";
  private static final String URL_BUNDLES = "/system/console/bundles";

  @javax.annotation.Resource
  private ManagedExecutorService mes;

  public Observable<JsonArray> getBundlesJsonArray() {
    Observable<Response> get = getObservableRespForPath(URL_BUNDLES_JSON);
    return get.map((response) -> {
      try (JsonReader reader = Json.createReader(
          new StringReader(response.readEntity(String.class)))) {
        JsonObject jsonObject = reader.readObject();
        return jsonObject.getJsonArray("data");
      } catch (Exception e) {
        throw Exceptions.propagate(e);
      }
    });
  }

  public Observable<String> getStatusConfigurations() {
    Observable<Response> get = getObservableRespForPath(
        URL_STATUS_CONFIGURATIONS_JSON);

    return get.map((response) -> response.readEntity(String.class));
  }

  public Observable<Boolean> managerIsConfigurated() {
    Observable<Response> get = getObservableRespForPath(URL_SERVICES_JSON);

    return get.map((response) -> {
      return response.readEntity(String.class)
          .contains(BundleConstants.MANAGER_API);
    });
  }

  public Observable<List<String>> getPidListBundles() {
    Observable<Response> get = getObservableRespForPath(URL_BUNDLES_JSON);

    return get.map((response) -> {
      List<String> listPids = new ArrayList<>();
      try (JsonReader reader = Json.createReader(
          new StringReader(response.readEntity(String.class)))) {
        JsonObject jsonObject = reader.readObject();
        JsonArray jArr = jsonObject.getJsonArray("data");

        for (int i = 0; i < jArr.size(); i++) {
          JsonObject jObj = jArr.getJsonObject(i);
          int id = jObj.getInt("id");
          if (id >= BundleConstants.COUNT_DEFAULT_BUNDLES) {
            listPids.add(jObj.getString("symbolicName"));
          }
        }
        return listPids;
      } catch (Exception e) {
        throw Exceptions.propagate(e);
      }
    });
  }

  public Observable<JsonObject> getConfiguration(String pid) {
    Observable<Response> post = postObservableRespForPath(URL_CONFIG_MGR + pid, null); // null???

    return post.map((response) -> {
      try (JsonReader reader = Json.createReader(
          new StringReader(response.readEntity(String.class)))) {
        JsonObject jsonObject = reader.readObject();
        return jsonObject.getJsonObject("properties");
      }
    });
  }

  public Observable<String> sendPostUninstall(String pid) {
    // unistall
    Form formUnistall = new Form();
    formUnistall.param("action", "uninstall");

    Observable<Response> postUnistall = postObservableRespForPath(URL_BUNDLES + "/" + pid,
        Entity.entity(formUnistall, MediaType.APPLICATION_FORM_URLENCODED));

    // delete config
    Form formDeleteConfig = new Form();
    formDeleteConfig.param("delete", "true");
    formDeleteConfig.param("apply", "true");

    Observable<Response> postDeleteConfig = postObservableRespForPath(URL_CONFIG_MGR + pid,
        Entity.entity(formDeleteConfig, MediaType.APPLICATION_FORM_URLENCODED));

    return Observable.zip(postUnistall, postDeleteConfig,
        (postUnll, postDelConfig) -> new String());
  }

  public Observable<String> sendPostUploadFile(InputStream inputStream,
                                               String filename, String pid,
                                               Map<String, String> parameters) {
    return Observable.create(o -> {
      try {
        postConfigureStart(pid, parameters);
        
        // загрузка файла
        InputStreamBody bin = new InputStreamBody(inputStream,
            filename);

        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("bundlefile", bin);
        reqEntity.addPart("action", new StringBody("install"));
        reqEntity.addPart("bundlestart", new StringBody("start"));
        reqEntity.addPart("bundlestartlevel", new StringBody("1"));
        postHttpClientQuery(URL_BUNDLES, reqEntity, Collections
            .list(new BasicHeader("Accept", "application/json")));

        o.onNext("");
      } catch (Exception e) {
        throw Exceptions.propagate(e);
      }
      o.onCompleted();
    }).subscribeOn(Schedulers.from(mes)).cast(String.class);

  }

  // TODO
  public Observable<String> sendPostConfigStart(String pid,
                                                Map<String, String> parameters) {
    return Observable.create(o -> {
      try {
        postConfigureStart(pid, parameters);
        o.onNext("");
      } catch (Exception e) {
        throw Exceptions.propagate(e);
      }
      o.onCompleted();
    }).subscribeOn(Schedulers.from(mes)).cast(String.class);
  }

  private void postConfigureStart(String pid, Map<String, String> parameters)
      throws IOException {
    // Загрузка конфигурации
    List<Header> headers = Collections.list(new BasicHeader("Content-Type",
        "application/x-www-form-urlencoded"));

    postHttpClientQuery(URL_CONFIG_MGR + pid,
        new UrlEncodedFormEntity(toListNameValue(parameters)), headers);

    /* Запуск бандла
    postHttpClientQuery(URL_BUNDLES + "/" + pid,
        new UrlEncodedFormEntity(Collections.list(new BasicNameValuePair("action", "start"))),
        headers); */
  }

  private void postHttpClientQuery(String path, HttpEntity entity,
                                     List<Header> headers) throws IOException {
    HttpClient httpclient = new DefaultHttpClient();
    HttpPost httppost = new HttpPost(UriBuilder
        .fromPath(config.deviceProxyEndpoint()).path(path).build());
    httppost.addHeader("Authorization", "Basic YWRtaW46YWRtaW4=");
    for (Header header : headers) {
      httppost.addHeader(header);
    }
    // httppost.addHeader("Content-Type",
    // "application/x-www-form-urlencoded");
    httppost.setEntity(entity);
    HttpResponse response = httpclient.execute(httppost);
    if (response.getStatusLine().getStatusCode() != 200
        && response.getStatusLine().getStatusCode() != 302) {
      logger.debug("Path: {}", path);
      logger.debug("Entity: {}", entity.toString());
      logger.debug(response.getStatusLine().toString());
    }
  }

  private List<NameValuePair> toListNameValue(Map<String, String> parameters) {
    List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();

    if (parameters != null) {
      for (Map.Entry<String, String> param : parameters.entrySet()) {
        urlParameters.add(new BasicNameValuePair(param.getKey(),
            param.getValue()));
      }
    }
    return urlParameters;
  }

  // not used
  public Observable<Response> postUpload(MultiPart multipart) {
    Observable<Response> post = Rx.newClient(RxObservableInvoker.class, mes)
        .register(MultiPartFeature.class)
        .target(UriBuilder.fromPath(config.deviceProxyEndpoint()).path(URL_BUNDLES).build())
        .request(MediaType.APPLICATION_JSON)
        .header("Authorization", "Basic YWRtaW46YWRtaW4=").rx()
        .post(Entity.entity(multipart, MediaType.MULTIPART_FORM_DATA_TYPE));

    return post;
  }

  private Observable<Response> getObservableRespForPath(String path) {
    return Rx.newClient(RxObservableInvoker.class, mes)
        .target(UriBuilder.fromPath(config.deviceProxyEndpoint()).path(path).build())
        .request(MediaType.APPLICATION_JSON)
        .header("Authorization", "Basic YWRtaW46YWRtaW4=").rx().get();
  }

  private Observable<Response> postObservableRespForPath(String path, Entity<?> ent) {
    return Rx.newClient(RxObservableInvoker.class, mes)
        .target(UriBuilder.fromPath(config.deviceProxyEndpoint()).path(path).build())
        .request(MediaType.APPLICATION_JSON)
        .header("Authorization", "Basic YWRtaW46YWRtaW4=").rx()
        .post(ent);
  }

}
