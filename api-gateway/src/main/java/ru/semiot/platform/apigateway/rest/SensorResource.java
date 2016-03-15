package ru.semiot.platform.apigateway.rest;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.utils.JsonUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
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
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.platform.apigateway.ContextProvider;
import ru.semiot.platform.apigateway.SPARQLQueryService;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.commons.namespaces.Proto;
import ru.semiot.commons.namespaces.VOID;
import ru.semiot.platform.apigateway.TSDBQueryService;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import rx.Observable;
import rx.exceptions.Exceptions;
import static ru.semiot.platform.apigateway.rest.ResourceHelper.*;
import ru.semiot.platform.apigateway.utils.MapBuilder;
import ru.semiot.platform.apigateway.utils.URIUtils;

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
    private static final String QUERY_SENSOR_URI_BY_ID
            = "SELECT ?sensorUri ?systemId {"
            + " ?systemUri ssn:hasSubSystem ?sensorUri ;"
            + "   dcterms:identifier ?systemId ."
            + " ?sensorUri dcterms:identifier \"${SENSOR_ID}\"^^xsd:string ."
            + "} LIMIT 1";

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

    @GET
    @Path("{id}/observations")
    public void observations(@Suspended final AsyncResponse response,
            @PathParam("id") String sensorId,
            @QueryParam("start") String start,
            @QueryParam("end") String end)
            throws IOException {
        URI root = uriInfo.getRequestUri();
        Map params = MapBuilder.newMap()
                .put(ContextProvider.VAR_ROOT_URL, URIUtils.extractRootURL(root))
                .put(ContextProvider.VAR_SENSOR_ID, sensorId)
                .build();
        if (Strings.isNullOrEmpty(start)) {
            sparqlQuery.select(QUERY_SENSOR_URI_BY_ID.replace("${SENSOR_ID}", sensorId)).map((rs) -> {
                if (rs.hasNext()) {
                    QuerySolution qs = rs.next();
                    String sensorUri = qs.getResource("sensorUri").getURI();
                    String systemId = qs.getLiteral("systemId").getString();
                    try {
                        return tsdbQuery.queryTimeOfLatestBySensorUri(systemId, sensorUri)
                                .toBlocking().single();
                    } catch (UnsupportedEncodingException ex) {
                        throw Exceptions.propagate(ex);
                    }
                } else {
                    throw Exceptions.propagate(new WebApplicationException(
                            Response.Status.NOT_FOUND));
                }
            }).subscribe((result) -> {
                if (result != null) {
                    logger.info("exists");
                    response.resume(Response.seeOther(UriBuilder.fromUri(root)
                            .queryParam("start", result)
                            .build())
                            .build());
                } else {
                    logger.info("not exists");
                    try {
                        Map<String, Object> frame = contextProvider.getFrame(
                                ContextProvider.SENSOR_OBSERVATIONS_COLLECTION, root);
                        params.put(ContextProvider.VAR_QUERY_PARAMS, "?noparams");
                        Model model = contextProvider.getRDFModel(
                                ContextProvider.SENSOR_OBSERVATIONS_COLLECTION, params);
                        Resource view = RDFUtils.subjectWithProperty(
                                model, RDF.type, Hydra.PartialCollectionView);
                        model.remove(view, null, null);

                        response.resume(JsonUtils.toPrettyString(
                                RDFUtils.toJsonLdCompact(model, frame)));
                    } catch (Throwable ex) {
                        response.resume(ex);
                    }
                }
            }, resumeOnError(response));
        } else {
            logger.info("find");
            Map<String, Object> frame = contextProvider.getFrame(
                    ContextProvider.SENSOR_OBSERVATIONS_PARTIAL_COLLECTION, root);

            String queryParams = "?start=" + start;
            if (!Strings.isNullOrEmpty(end)) {
                queryParams += "&end=" + end;
            }
            params.put(ContextProvider.VAR_QUERY_PARAMS, queryParams);

            Model model = contextProvider.getRDFModel(
                    ContextProvider.SENSOR_OBSERVATIONS_COLLECTION, params);
            queryTSDBBySensorUri(sensorId, start, end).map((result) -> {
                model.add(result);
                Resource collection = model.listSubjectsWithProperty(
                        RDF.type, Hydra.PartialCollectionView).next();
                ResIterator iter = model.listSubjectsWithProperty(
                        RDF.type, SSN.Observaton);
                while (iter.hasNext()) {
                    Resource obs = iter.next();
                    model.add(collection, Hydra.member, obs);
                }
                try {
                    return JsonUtils.toPrettyString(
                            RDFUtils.toJsonLdCompact(model, frame));
                } catch (JsonLdError | IOException ex) {
                    throw Exceptions.propagate(ex);
                }
            }).subscribe(resume(response));
        }
    }

    private Observable<Model> queryTSDBBySensorUri(String sensorId, String start, String end) {
        return sparqlQuery.select(QUERY_SENSOR_URI_BY_ID.replace("${SENSOR_ID}", sensorId))
                .map((ResultSet rs) -> {
                    if (rs.hasNext()) {
                        QuerySolution qs = rs.next();
                        String sensorUri = qs.getResource("sensorUri").getURI();
                        String systemId = qs.getLiteral("systemId").getString();
                        try {
                            return tsdbQuery.queryBySensorUri(systemId, sensorUri, start, end)
                            .toBlocking().single();
                        } catch (UnsupportedEncodingException ex) {
                            throw Exceptions.propagate(ex);
                        }
                    } else {
                        throw Exceptions.propagate(new WebApplicationException(
                                        Response.Status.NOT_FOUND));
                    }
                });
    }

}
