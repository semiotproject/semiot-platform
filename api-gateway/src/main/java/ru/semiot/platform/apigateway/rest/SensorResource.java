package ru.semiot.platform.apigateway.rest;

import static ru.semiot.commons.restapi.AsyncResponseHelper.resume;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.utils.JsonUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.commons.namespaces.VOID;
import ru.semiot.commons.rdf.ModelJsonLdUtils;
import ru.semiot.commons.restapi.MediaType;
import ru.semiot.platform.apigateway.beans.impl.ContextProvider;
import ru.semiot.platform.apigateway.beans.impl.SPARQLQueryService;
import rx.Observable;
import rx.exceptions.Exceptions;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("/sensors")
@Stateless
public class SensorResource {

  private static final Logger logger = LoggerFactory.getLogger(SensorResource.class);
  private static final String QUERY_GET_ALL_SENSORS
      = "SELECT DISTINCT ?uri ?prototype ?id {"
      + "  ?uri a ssn:SensingDevice, proto:Individual ;"
      + "    proto:hasPrototype ?prototype ."
      + "}";
  private static final String QUERY_GET_SENSOR_PROTOTYPES
      = "SELECT DISTINCT ?prototype {"
      + " ?uri a ssn:SensingDevice, proto:Individual ;"
      + "     proto:hasPrototype ?prototype ."
      + "}";

  private static final String VAR_URI = "uri";
  private static final String VAR_PROTOTYPE = "prototype";

  @Inject
  private ContextProvider contextProvider;

  @Inject
  private SPARQLQueryService sparqlQuery;

  @Context
  private UriInfo uriInfo;

  public SensorResource() {
  }

  @GET
  @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
  public void listSensors(@Suspended final AsyncResponse response)
      throws IOException {
    URI root = uriInfo.getRequestUri();
    final Model model = contextProvider.getRDFModel(ContextProvider.SENSOR_COLLECTION, root);
    final Map<String, Object> frame = contextProvider
        .getFrame(ContextProvider.SENSOR_COLLECTION, root);

    Observable<Void> prototypes = sparqlQuery.select(QUERY_GET_SENSOR_PROTOTYPES)
        .map((ResultSet rs) -> {
          while (rs.hasNext()) {
            Resource prototype = rs.next().getResource(VAR_PROTOTYPE);
            Resource prototypeResource = ResourceUtils.createResourceFromClass(
                root, prototype.getLocalName());
            Resource collection = model.listResourcesWithProperty(
                RDF.type, Hydra.Collection).next();

            Resource restriction = ResourceFactory.createResource();
            model.add(collection, VOID.classPartition, restriction);
            model.add(restriction, VOID.clazz, prototypeResource);
            model.add(collection, VOID.classPartition, restriction);
          }

          return null;
        });
    Observable<String> sensors = sparqlQuery.select(QUERY_GET_ALL_SENSORS).map((ResultSet rs) -> {
      while (rs.hasNext()) {
        QuerySolution qs = rs.next();
        Resource sensor = qs.getResource(VAR_URI);
        Resource prototype = qs.getResource(VAR_PROTOTYPE);

        Resource collection = model.listResourcesWithProperty(RDF.type, Hydra.Collection).next();
        model.add(collection, Hydra.member, sensor);
        model.add(sensor, RDF.type, ResourceUtils.createResourceFromClass(
            root, prototype.getLocalName()));
      }

      try {
        return JsonUtils.toPrettyString(ModelJsonLdUtils.toJsonLdCompact(model, frame));
      } catch (JsonLdError | IOException ex) {
        throw Exceptions.propagate(ex);
      }
    });

    Observable.zip(sensors, prototypes, (a, b) -> a).subscribe(resume(response));
  }

}
