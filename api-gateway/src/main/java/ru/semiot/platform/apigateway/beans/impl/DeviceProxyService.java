package ru.semiot.platform.apigateway.beans.impl;

import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFLanguages;
import org.glassfish.jersey.client.rx.Rx;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.restapi.MediaType;
import ru.semiot.platform.apigateway.ServerConfig;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import rx.Observable;
import rx.exceptions.Exceptions;

import java.net.URI;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Singleton
public class DeviceProxyService {

  private static final Logger logger = LoggerFactory.getLogger(DeviceProxyService.class);
  private static final ServerConfig config = ConfigFactory.create(ServerConfig.class);

  @Resource
  ManagedExecutorService mes;

  public Observable<Model> executeCommand(String systemId, Model command) {
    URI uri = UriBuilder
        .fromPath(config.deviceProxyEndpoint())
        .path(config.deviceProxyCommandAPI())
        .queryParam("system_id", systemId)
        .build();

    logger.debug("{}", uri);

    Observable<Response> post = Rx.newClient(RxObservableInvoker.class, mes)
        .target(uri)
        .request().rx()
        .post(Entity.entity(
            RDFUtils.toString(command, RDFLanguages.TURTLE),
            MediaType.TEXT_TURTLE));

    return post.map(response -> {
      if (response.getStatus() == 200) {
        return RDFUtils.toModel(response.readEntity(String.class), RDFLanguages.JSONLD);
      } else {
        throw Exceptions.propagate(new WebApplicationException(
            Response.status(response.getStatus())
                .entity(response.readEntity(String.class))
                .build()));
      }
    });
  }
}
