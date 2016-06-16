package ru.semiot.platform.apigateway.rest;

import static ru.semiot.commons.restapi.AsyncResponseHelper.resume;
import static ru.semiot.commons.restapi.AsyncResponseHelper.resumeOnError;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.utils.JsonUtils;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.commons.rdf.ModelJsonLdUtils;
import ru.semiot.commons.restapi.MediaType;
import ru.semiot.platform.apigateway.ServerConfig;
import ru.semiot.platform.apigateway.beans.TSDBQueryService;
import ru.semiot.platform.apigateway.beans.impl.ContextProvider;
import ru.semiot.platform.apigateway.beans.impl.SPARQLQueryService;
import ru.semiot.platform.apigateway.utils.MapBuilder;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import ru.semiot.platform.apigateway.utils.URIUtils;
import rx.exceptions.Exceptions;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
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

@Path("/systems/{system_id}/observations")
public class SystemObservationsResource extends AbstractSystemResource {

  private static final ServerConfig config = ConfigFactory.create(ServerConfig.class);
  private static final Logger logger = LoggerFactory.getLogger(SystemObservationsResource.class);
  private static final String QUERY_GET_SYSTEM_SENSORS = "SELECT DISTINCT ?sensor_id {"
      + " ?uri a ssn:System ;"
      + "     dcterms:identifier \"${SYSTEM_ID}\"^^xsd:string ;"
      + "     ssn:hasSubSystem ?sensor ."
      + " ?sensor dcterms:identifier ?sensor_id }";
  private static final String VAR_SENSOR_ID = "sensor_id";
  private static final String TOPIC_SENSOR_TEMPLATE = "${SYSTEM_ID}.observations.${SENSOR_ID}";
  private static final String TOPIC_SYSTEM_TEMPLATE = "${SYSTEM_ID}.observations";

  public SystemObservationsResource() {
    super();
  }

  @Inject
  private SPARQLQueryService sparqlQuery;
  @Inject
  private TSDBQueryService tsdbQuery;
  @Inject
  private ContextProvider contextProvider;
  @Context
  UriInfo uriInfo;

  @GET
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_LD_JSON})
  public void observations(@Suspended final AsyncResponse response,
      @PathParam("system_id") String systemId, @QueryParam("sensor_id") String sensorId,
      @QueryParam("start") String start, @QueryParam("end") String end)
      throws IOException {
    if (Strings.isNullOrEmpty(systemId)) {
      response.resume(Response.status(Response.Status.NOT_FOUND).build());
    }
    
    URI root = uriInfo.getRequestUri();
    String rootUrl = URIUtils.extractRootURL(root);
    Map params = MapBuilder.newMap()
        .put(ContextProvider.VAR_ROOT_URL, rootUrl)
        .put(ContextProvider.VAR_WAMP_URL, UriBuilder
            .fromUri(rootUrl + config.wampPublicPath())
            .scheme(config.wampProtocolScheme()).build())
        .put(ContextProvider.VAR_SYSTEM_ID, systemId).build();
    List<String> listSensorId;
    if (sensorId == null) {
      listSensorId = null;
      params.put(ContextProvider.VAR_TOPIC_NAME,
          TOPIC_SYSTEM_TEMPLATE.replace("${SYSTEM_ID}", systemId));
    } else {
      listSensorId = Collections.list(sensorId);
      params.put(ContextProvider.VAR_TOPIC_NAME, TOPIC_SENSOR_TEMPLATE
          .replace("${SYSTEM_ID}", systemId).replace("${SENSOR_ID}", sensorId));
    }
    if (Strings.isNullOrEmpty(start)) {
      tsdbQuery.queryDateTimeOfLatestObservation(
          systemId, getListSensorsId(systemId, listSensorId)).subscribe((result) -> {
        if (StringUtils.isNotBlank(result)) {
          UriBuilder uriBuilder = UriBuilder.fromUri(root).queryParam("start", result);
          response.resume(Response.seeOther(uriBuilder.build()).build());
        } else {
          try {
            Map<String, Object> frame = contextProvider.getFrame(
                ContextProvider.SYSTEM_OBSERVATIONS_COLLECTION, root);
            params.put(ContextProvider.VAR_QUERY_PARAMS, "?noparams");
            Model model = contextProvider.getRDFModel(
                ContextProvider.SYSTEM_OBSERVATIONS_COLLECTION, params);
            Resource view = RDFUtils.subjectWithProperty(
                model, RDF.type, Hydra.PartialCollectionView);
            model.removeAll(view, null, null);

            response.resume(JsonUtils.toPrettyString(ModelJsonLdUtils.toJsonLdCompact(model, frame)));
          } catch (Throwable ex) {
            response.resume(ex);
          }
        }
      }, resumeOnError(response));
    } else {
      Map<String, Object> frame = contextProvider.getFrame(
          ContextProvider.SYSTEM_OBSERVATIONS_PARTIAL_COLLECTION, root);

      String queryParams = "?";
      if (listSensorId != null && !listSensorId.isEmpty()) {
        queryParams += "sensor_id=" + StringUtils.join(listSensorId, ',') + "&";
      }
      queryParams += "start=" + start;
      if (!Strings.isNullOrEmpty(end)) {
        queryParams += "&end=" + end;
      }
      params.put(ContextProvider.VAR_QUERY_PARAMS, queryParams);

      Model model = contextProvider.getRDFModel(
          ContextProvider.SYSTEM_OBSERVATIONS_COLLECTION, params);

      tsdbQuery.queryObservationsByRange(systemId,
          getListSensorsId(systemId, listSensorId), start, end).map((result) -> {
        model.add(result);
        Resource collection = model.listSubjectsWithProperty(
            RDF.type, Hydra.PartialCollectionView).next();
        ResIterator iter = model.listSubjectsWithProperty(RDF.type, SSN.Observaton);
        while (iter.hasNext()) {
          Resource obs = iter.next();
          model.add(collection, Hydra.member, obs);
        }
        try {
          return JsonUtils.toPrettyString(ModelJsonLdUtils.toJsonLdCompact(model, frame));
        } catch (JsonLdError | IOException ex) {
          throw Exceptions.propagate(ex);
        }
      }).map((json) -> Response.ok(json).build()).subscribe(resume(response));
    }
  }

  private List<String> getListSensorsId(String systemId,
      List<String> sensorsId) {
    if (sensorsId == null || sensorsId.isEmpty()) {
      return rsToList(sparqlQuery.select(QUERY_GET_SYSTEM_SENSORS.replace("${SYSTEM_ID}", systemId))
          .toBlocking().single());
    } else {
      return sensorsId;
    }
  }

  private List<String> rsToList(ResultSet rs) {
    List<String> list = new ArrayList<String>();
    while (rs.hasNext()) {
      QuerySolution qs = rs.next();
      list.add(qs.getLiteral(VAR_SENSOR_ID).getString());
    }
    return list;
  }

}
