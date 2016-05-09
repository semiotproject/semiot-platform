package ru.semiot.platform.apigateway.rest;

import static ru.semiot.commons.restapi.AsyncResponseHelper.resume;
import static ru.semiot.commons.restapi.AsyncResponseHelper.resumeOnError;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.utils.JsonUtils;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.commons.namespaces.SEMIOT;
import ru.semiot.commons.rdf.ModelJsonLdUtils;
import ru.semiot.commons.restapi.MediaType;
import ru.semiot.platform.apigateway.ServerConfig;
import ru.semiot.platform.apigateway.beans.TSDBQueryService;
import ru.semiot.platform.apigateway.beans.impl.ContextProvider;
import ru.semiot.platform.apigateway.beans.impl.DeviceProxyService;
import ru.semiot.platform.apigateway.utils.MapBuilder;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import ru.semiot.platform.apigateway.utils.URIUtils;
import rx.exceptions.Exceptions;

import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

@Path("/systems/{system_id}/commandResults")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_LD_JSON})
@Stateless
public class SystemCommandResultsResource {

  private static final Logger logger = LoggerFactory.getLogger(SystemCommandResultsResource.class);
  private static final ServerConfig config = ConfigFactory.create(ServerConfig.class);

  @Inject
  ContextProvider contextProvider;
  @Inject
  TSDBQueryService tsdb;
  @Inject
  DeviceProxyService dps;
  @Context
  UriInfo uriInfo;

  @GET
  public void getCommandResults(@Suspended AsyncResponse response,
      @PathParam("system_id") String systemId,
      @QueryParam("start") ZonedDateTime start,
      @QueryParam("end") ZonedDateTime end) {
    URI root = uriInfo.getRequestUri();
    String rootURL = URIUtils.extractRootURL(root);
    Map params = MapBuilder.newMap()
        .put(ContextProvider.VAR_ROOT_URL, rootURL)
        .put(ContextProvider.VAR_WAMP_URL, UriBuilder
            .fromUri(rootURL + config.wampPublicPath())
            .scheme(config.wampProtocolScheme()).build())
        .put(ContextProvider.VAR_SYSTEM_ID, systemId)
        .build();
    if (Strings.isNullOrEmpty(systemId)) {
      response.resume(Response.status(Response.Status.NOT_FOUND).build());
    }

    if (start == null) {
      tsdb.queryDateTimeOfLatestCommandResult(systemId).subscribe((dateTime) -> {
        if (dateTime != null) {
          response.resume(Response.seeOther(UriBuilder.fromUri(root)
              .queryParam("start", dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
              .build())
              .build());
        } else {
          try {
            Map<String, Object> frame = contextProvider.getFrame(
                ContextProvider.SYSTEM_COMMANDRESULTS_COLLECTION, rootURL);
            params.put(ContextProvider.VAR_QUERY_PARAMS, "?noparams");
            Model model = contextProvider.getRDFModel(
                ContextProvider.SYSTEM_COMMANDRESULTS_COLLECTION, params);
            Resource view = RDFUtils.subjectWithProperty(
                model, RDF.type, Hydra.PartialCollectionView);
            model.removeAll(view, null, null);

            response.resume(JsonUtils.toPrettyString(
                ModelJsonLdUtils.toJsonLdCompact(model, frame)));
          } catch (Throwable ex) {
            resumeOnError(response, ex);
          }
        }
      }, resumeOnError(response));
    } else {
      String queryParams = "?start=" + start;
      if (end != null) {
        queryParams += "&end=" + end;
      }
      params.put(ContextProvider.VAR_QUERY_PARAMS, queryParams);

      Model model = contextProvider.getRDFModel(
          ContextProvider.SYSTEM_COMMANDRESULTS_COLLECTION, params);
      tsdb.queryCommandResultsByRange(systemId, start, end).subscribe((result) -> {
        try {
          Map<String, Object> frame = contextProvider.getFrame(
              ContextProvider.SYSTEM_COMMANDRESULTS_PARTIAL_COLLECTION, rootURL);
          model.add(result);
          Resource collection = model.listSubjectsWithProperty(
              RDF.type, Hydra.PartialCollectionView).next();
          ResIterator iter = model.listSubjectsWithProperty(RDF.type, SEMIOT.CommandResult);
          while (iter.hasNext()) {
            Resource obs = iter.next();
            model.add(collection, Hydra.member, obs);
          }

          response.resume(JsonUtils.toPrettyString(ModelJsonLdUtils.toJsonLdCompact(model, frame)));
        } catch (Throwable e) {
          resumeOnError(response, e);
        }
      }, resumeOnError(response));
    }
  }

  @POST
  @Consumes({MediaType.APPLICATION_LD_JSON, MediaType.TEXT_TURTLE})
  public void executeCommand(@Suspended AsyncResponse response,
      @PathParam("system_id") String systemId, Model command) {
    try {
      URI root = uriInfo.getRequestUri();
      Map<String, Object> frame = contextProvider.getFrame(ContextProvider.COMMANDRESULT, root);

      if (Strings.isNullOrEmpty(systemId)) {
        response.resume(Response.status(Response.Status.NOT_FOUND).build());
      }

      if (command == null || command.isEmpty()) {
        response.resume(Response.status(Response.Status.BAD_REQUEST).build());
      } else {
        dps.executeCommand(systemId, command).map((commandResult) -> {
          try {
            return Response.ok(ModelJsonLdUtils.toJsonLdCompact(commandResult, frame)).build();
          } catch (JsonLdError | IOException ex) {
            throw Exceptions.propagate(ex);
          }
        }).subscribe(resume(response));
      }
    } catch (Throwable ex) {
      resumeOnError(response, ex);
    }
  }
}
