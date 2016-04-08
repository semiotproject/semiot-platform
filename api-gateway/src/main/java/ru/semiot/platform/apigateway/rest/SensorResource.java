package ru.semiot.platform.apigateway.rest;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.utils.JsonUtils;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.commons.namespaces.Proto;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.commons.namespaces.VOID;
import ru.semiot.platform.apigateway.ContextProvider;
import ru.semiot.platform.apigateway.SPARQLQueryService;
import ru.semiot.platform.apigateway.beans.TSDBQueryService;
import ru.semiot.platform.apigateway.utils.MapBuilder;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import ru.semiot.platform.apigateway.utils.URIUtils;
import rx.Observable;
import rx.exceptions.Exceptions;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;

import static ru.semiot.platform.apigateway.rest.ResourceHelper.resume;
import static ru.semiot.platform.apigateway.rest.ResourceHelper.resumeOnError;

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
    private ContextProvider contextProvider;

    @Inject
    private SPARQLQueryService sparqlQuery;

    @Inject
    private TSDBQueryService tsdbQuery;

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

                Resource collection = model.listResourcesWithProperty(
                        RDF.type, Hydra.Collection).next();
                model.add(collection, Hydra.member, sensor);
                model.add(sensor, RDF.type, ResourceUtils.createResourceFromClass(
                        root, prototype.getLocalName()));
            }

            try {
                return JsonUtils.toPrettyString(
                        RDFUtils.toJsonLdCompact(model, frame));
            } catch (JsonLdError | IOException ex) {
                throw Exceptions.propagate(ex);
            }

        });

        Observable.zip(sensors, prototypes, (a, b) -> {
            return a;
        }).subscribe(resume(response));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
    public void getSensor(@Suspended final AsyncResponse response,
                          @PathParam("id") String id) throws IOException {
        URI root = uriInfo.getRequestUri();
        Model model = contextProvider.getRDFModel(ContextProvider.SENSOR_SINGLE,
                MapBuilder.newMap()
                        .put(ContextProvider.VAR_ROOT_URL, URIUtils.extractRootURL(root))
                        .put(ContextProvider.VAR_SENSOR_ID, id)
                        .build());
        Map<String, Object> frame = contextProvider.getFrame(
                ContextProvider.SENSOR_SINGLE, root);

        sparqlQuery.describe(QUERY_DESCRIBE_SENSOR.replace("${SENSOR_ID}", id))
                .map((Model result) -> {
                    model.add(result);
                    try {
                        Resource sensor = model.listResourcesWithProperty(
                                RDF.type, SSN.SensingDevice).next();
                        Resource prototype = model.listObjectsOfProperty(
                                sensor, Proto.hasPrototype).next().asResource();
                        Resource prototypeResource = ResourceUtils
                                .createResourceFromClass(root, prototype.getLocalName());
                        model.add(sensor, RDF.type, prototypeResource);

                        return JsonUtils.toPrettyString(
                                RDFUtils.toJsonLdCompact(model, frame));
                    } catch (JsonLdError | IOException ex) {
                        throw Exceptions.propagate(ex);
                    }
                }).subscribe(resume(response));
    }

}
