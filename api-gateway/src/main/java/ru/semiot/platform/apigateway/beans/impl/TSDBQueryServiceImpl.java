package ru.semiot.platform.apigateway.beans.impl;

import org.aeonbits.owner.ConfigFactory;
import org.apache.http.HttpStatus;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.glassfish.jersey.client.rx.Rx;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.DUL;
import ru.semiot.platform.apigateway.ServerConfig;
import ru.semiot.platform.apigateway.beans.TSDBQueryService;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import rx.Observable;

import java.io.StringReader;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.inject.Default;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Singleton
@Default
public class TSDBQueryServiceImpl implements TSDBQueryService {

  private static final Logger logger = LoggerFactory
      .getLogger(TSDBQueryServiceImpl.class);
  private static final ServerConfig config = ConfigFactory
      .create(ServerConfig.class);

  private static final String QUERY_OBSERVATIONS = "/observations";
  private static final String QUERY_OBSERVATIONS_LATEST = "/observations/latest";
  private static final String QUERY_TIME_OBSERVATIONS_LATEST = "/observations/latest/time";
  private static final String QUERY_REMOVE = "/remove";
  private static final String PARAM_SYSTEM_ID = "system_id";
  private static final String PARAM_SENSOR_ID = "sensor_id";
  private static final String PARAM_START = "start";
  private static final String PARAM_END = "end";
  private static final String QUERY_SETTINGS = "/settings";

  @javax.annotation.Resource
  private ManagedExecutorService mes;

  @Override
  public Observable<String> queryTimeOfLatestBySystemId(String systemId, List<String> sensorsId) {
    Observable<Response> get = Rx.newClient(RxObservableInvoker.class, mes)
        .target(UriBuilder.fromPath(config.tsdbEndpoint())
            .path(QUERY_TIME_OBSERVATIONS_LATEST)
            .queryParam(PARAM_SYSTEM_ID, systemId)
            .queryParam(PARAM_SENSOR_ID, sensorsId.toArray()).build())
        .request().rx().get();

    return get.map((response) -> response.readEntity(String.class));
  }

  @Override
  public Observable<Model> queryLatestBySystemId(String systemId, List<String> sensorsId) {
    Observable<Response> get = Rx.newClient(RxObservableInvoker.class, mes)
        .target(UriBuilder.fromPath(config.tsdbEndpoint())
            .path(QUERY_OBSERVATIONS_LATEST)
            .queryParam(PARAM_SYSTEM_ID, systemId)
            .queryParam(PARAM_SENSOR_ID, sensorsId.toArray())
            .build())
        .request().rx().get();

    return responseToModel(get);
  }

  @Override
  public Observable<Model> queryBySystemId(String systemId, List<String> sensorsId,
                                           String start, String end) {
    if (Strings.isNullOrEmpty(start)) {
      throw new IllegalArgumentException();
    }

    UriBuilder uriBuilder = UriBuilder.fromPath(config.tsdbEndpoint())
        .path(QUERY_OBSERVATIONS).queryParam(PARAM_SYSTEM_ID, systemId)
        .queryParam(PARAM_SENSOR_ID, sensorsId.toArray())
        .queryParam(PARAM_START, start);

    if (!Strings.isNullOrEmpty(end)) {
      uriBuilder.queryParam(PARAM_END, end);
    }

    Observable<Response> get = Rx.newClient(RxObservableInvoker.class, mes)
        .target(uriBuilder.build()).request().rx().get();

    return responseToModel(get);
  }

  Observable<Model> responseToModel(Observable<Response> get) {
    return get.map((response) -> {
      Model model = ModelFactory.createDefaultModel();
      try {
        if (response.getStatus() == HttpStatus.SC_OK) {
          StringReader reader = new StringReader(
              response.readEntity(String.class));
          model.read(reader, null, RDFLanguages.strLangJSONLD);
        }
      } catch (RiotException re) {
        logger.error(re.getMessage(), re);
      }
      return model;
    });
  }

  @Override
  public Observable<Response> remove(JsonArray jsonArray) {
    return Rx.newClient(RxObservableInvoker.class, mes)
        .target(UriBuilder.fromPath(config.tsdbEndpoint())
            .path(QUERY_REMOVE))
        .request().rx().post(Entity.entity(jsonArray.toString(),
            MediaType.TEXT_PLAIN));
  }

  @Override
  public Observable<ZonedDateTime> queryDateTimeOfLatestActuation(
      String systemId) {
    URI uri = UriBuilder.fromPath(config.tsdbEndpoint())
        .path(config.tsdbActuatorsLatestPath())
        .queryParam("system_id", systemId).build();

    logger.debug("queryDateTimeOfLatestActuation: URL {}", uri);

    Observable<Response> result = Rx.newClient(RxObservableInvoker.class, mes)
        .target(uri).request().rx().get();

    return result.map((response -> {
      if (response.getStatus() == 200) {
        Model model = RDFUtils.toModel(
            response.readEntity(String.class), RDFLanguages.JSONLD);
        if (model.isEmpty()) {
          return null;
        } else {
          Literal dateTime = (Literal) model
              .listObjectsOfProperty(DUL.hasEventTime).next();

          return ZonedDateTime.parse(dateTime.getLexicalForm(),
              DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
      } else {
        logger.debug("None 200 nor 404");
        return null;
        // throw Exceptions.propagate(new
        // WebApplicationException(Response
        // .status(response.getStatus())
        // .entity(response.readEntity(String.class))
        // .build()));
      }
    }));
  }

  @Override
  public Observable<Model> queryActuationsByRange(String systemId,
                                                  ZonedDateTime start, ZonedDateTime end) {
    UriBuilder uriBuilder = UriBuilder.fromPath(config.tsdbEndpoint())
        .path(config.tsdbActuatorsPath())
        .queryParam("system_id", systemId).queryParam("start",
            start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    if (end != null) {
      uriBuilder.queryParam("end",
          end.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
    URI uri = uriBuilder.build();

    logger.debug("queryActuationsByRange: URL {}", uri);

    Observable<Response> result = Rx
        .newClient(RxObservableInvoker.class, mes).target(uri).request()
        .rx().get();

    return result.map((response -> {
      if (response.getStatus() == 200) {
        return RDFUtils.toModel(response.readEntity(String.class),
            RDFLanguages.JSONLD);
      } else {
        logger.debug("None 200 nor 404");
        return null;
        // throw Exceptions.propagate(new
        // WebApplicationException(Response
        // .status(response.getStatus())
        // .entity(response.readEntity(String.class))
        // .build()));
      }
    }));
  }

  @Override
  public Observable<Response> sendSettingsAsPost(JsonObject json) {
    return Rx.newClient(RxObservableInvoker.class, mes)
        .target(UriBuilder.fromPath(config.tsdbEndpoint())
            .path(QUERY_SETTINGS))
        .request().rx().post(Entity.entity(json.toString(),
            MediaType.APPLICATION_JSON));
  }
}
