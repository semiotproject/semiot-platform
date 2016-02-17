package ru.semiot.platform.apigateway.rest;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.platform.apigateway.ContextProvider;
import ru.semiot.platform.apigateway.SPARQLQueryService;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.commons.namespaces.Proto;
import ru.semiot.commons.namespaces.VOID;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import rx.Observable;
import rx.exceptions.Exceptions;

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

    private static final String QUERY_DESCRIBE_SENSOR
            = "CONSTRUCT {"
            + "	?sensor ?p ?o ."
            + " ?o ?o_p ?o_o ."
            + "} WHERE {"
            + " ?sensor ?p ?o ;"
            + "  dcterms:identifier \"${SENSOR_ID}\"^^xsd:string ."
            + " OPTIONAL {"
            + "  ?o ?o_p ?o_o ."
            + "  FILTER isBlank(?o)"
            + " }"
            + "}";

    private static final String VAR_URI = "uri";
    private static final String VAR_PROTOTYPE = "prototype";

    @Inject
    ContextProvider contextProvider;

    @Inject
    SPARQLQueryService query;

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
        final Map<String, Object> frame = contextProvider.getFrame(ContextProvider.SENSOR_COLLECTION, root);

        Observable<Void> prototypes = query.select(QUERY_GET_SENSOR_PROTOTYPES)
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
        Observable<String> sensors = query.select(QUERY_GET_ALL_SENSORS).map((ResultSet rs) -> {
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                Resource sensor = qs.getResource(VAR_URI);
                Resource prototype = qs.getResource(VAR_PROTOTYPE);

                Resource collection = model.listResourcesWithProperty(
                        RDF.type, Hydra.Collection).next();
                model.add(collection, Hydra.member, sensor);
                model.add(sensor, RDF.type, ResourceUtils.createResourceFromClass(
                        root, prototype.getLocalName()));
            }

            try {
                Object result = JsonLdProcessor.frame(
                        RDFUtils.toJsonLd(model), frame, new JsonLdOptions());

                return JsonUtils.toString(result);
            } catch (JsonLdError | IOException ex) {
                throw Exceptions.propagate(ex);
            }

        });

        Observable.zip(sensors, prototypes, (a, b) -> {
            return a;
        }).subscribe((o) -> {
            response.resume(o);
        }, (e) -> {
            logger.warn(e.getMessage(), e);

            response.resume(e);
        });
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
    public void getSensor(@Suspended final AsyncResponse response,
            @PathParam("id") String id) throws IOException {
        URI root = uriInfo.getRequestUri();
        final Map<String, Object> frame = contextProvider.getFrame(ContextProvider.SENSOR_SINGLE, root);

        query.describe(QUERY_DESCRIBE_SENSOR.replace("${SENSOR_ID}", id))
                .map((Model model) -> {
                    try {
                        Resource sensor = model.listResourcesWithProperty(
                                RDF.type, SSN.SensingDevice).next();
                        Resource prototype = model.listObjectsOfProperty(
                                sensor, Proto.hasPrototype).next().asResource();
                        Resource prototypeResource = ResourceUtils
                        .createResourceFromClass(root, prototype.getLocalName());
                        model.add(sensor, RDF.type, prototypeResource);

                        Object result = JsonLdProcessor.frame(
                                RDFUtils.toJsonLd(model), frame, new JsonLdOptions());

                        return JsonUtils.toString(result);
                    } catch (JsonLdError | IOException ex) {
                        throw Exceptions.propagate(ex);
                    }
                }).subscribe((o) -> {
                    response.resume(o);
                }, (e) -> {
                    logger.warn(e.getMessage(), e);

                    response.resume(e);
                });
    }

}
