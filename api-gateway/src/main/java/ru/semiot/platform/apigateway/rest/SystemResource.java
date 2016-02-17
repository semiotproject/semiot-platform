package ru.semiot.platform.apigateway.rest;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

@Path("/systems")
@Stateless
public class SystemResource {

    private static final Logger logger = LoggerFactory.getLogger(SystemResource.class);
    private static final String QUERY_GET_ALL_SYSTEMS
            = "SELECT DISTINCT ?uri ?id ?prototype {"
            + " ?uri a ssn:System, proto:Individual ;"
            + "     dcterms:identifier ?id ;"
            + "     proto:hasPrototype ?prototype ."
            + "}";
    private static final String QUERY_GET_SYSTEM_PROTOTYPES
            = "SELECT DISTINCT ?prototype {"
            + " ?uri a ssn:System, proto:Individual ;"
            + "     proto:hasPrototype ?prototype ."
            + "}";
    private static final String QUERY_DESCRIBE_SYSTEM
            = "CONSTRUCT {"
            + "  ?system ?p ?o ."
            + "  ?o ?o_p ?o_o ."
            + "} WHERE {"
            + "  ?system ?p ?o ;"
            + "    dcterms:identifier \"${SYSTEM_ID}\"^^xsd:string ."
            + "  OPTIONAL {"
            + "    ?o ?o_p ?o_o ."
            + "    FILTER(?p != rdf:type)"
            + "  }"
            + "}";
    
    private static final String VAR_URI = "uri";
    private static final String VAR_PROTOTYPE = "prototype";

    public SystemResource() {
    }

    @Inject
    SPARQLQueryService query;

    @Inject
    ContextProvider contextProvider;

    @Context
    private UriInfo uriInfo;

    @GET
    @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
    public void listSystems(@Suspended final AsyncResponse response)
            throws JsonLdError, IOException {
        URI root = uriInfo.getRequestUri();
        final Model model = contextProvider.getRDFModel(ContextProvider.SYSTEM_COLLECTION, root);
        final Map<String, Object> frame = contextProvider.getFrame(ContextProvider.SYSTEM_COLLECTION, root);

        Observable<Void> prototypes = query.select(QUERY_GET_SYSTEM_PROTOTYPES)
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
        Observable<String> systems = query.select(QUERY_GET_ALL_SYSTEMS)
                .map((ResultSet rs) -> {
                    while (rs.hasNext()) {
                        QuerySolution qs = rs.next();
                        Resource system = qs.getResource(VAR_URI);
                        Resource prototype = qs.getResource(VAR_PROTOTYPE);

                        Resource collection = model.listResourcesWithProperty(
                                RDF.type, Hydra.Collection).next();
                        model.add(collection, Hydra.member, system);
                        model.add(system, RDF.type, ResourceUtils.createResourceFromClass(
                                        root, prototype.getLocalName()));
                    }

                    try {
                        Object result = JsonLdProcessor.frame(
                                RDFUtils.toJsonLd(model), frame, new JsonLdOptions());

                        return JsonUtils.toString(result);
                    } catch (IOException | JsonLdError e) {
                        throw Exceptions.propagate(e);
                    }
                });

        Observable.zip(systems, prototypes, (a, __) -> {
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
    public void getSystem(
            @Suspended final AsyncResponse response,
            @PathParam("id") String id) throws URISyntaxException, IOException {
        URI root = uriInfo.getRequestUri();
        final Map<String, Object> frame = contextProvider.getFrame(ContextProvider.SYSTEM_SINGLE, root);

        query.describe(QUERY_DESCRIBE_SYSTEM.replace("${SYSTEM_ID}", id))
                .map((Model model) -> {
                    try {
                        Resource system = model.listResourcesWithProperty(
                                RDF.type, SSN.System).next();
                        Resource prototype = model.listObjectsOfProperty(
                                system, Proto.hasPrototype).next().asResource();
                        Resource prototypeResource = ResourceUtils
                                .createResourceFromClass(root, prototype.getLocalName());
                        model.add(system, RDF.type, prototypeResource);
                        
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
