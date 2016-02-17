package ru.semiot.platform.apigateway.rest;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.platform.apigateway.ContextProvider;
import static ru.semiot.platform.apigateway.ContextProvider.*;
import ru.semiot.platform.apigateway.SPARQLQueryService;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.commons.namespaces.Proto;
import ru.semiot.commons.namespaces.SHACL;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import ru.semiot.platform.apigateway.utils.URIUtils;
import rx.Observable;
import rx.exceptions.Exceptions;

@Path("/")
@Stateless
public class RootResource {

    private static final Logger logger = LoggerFactory.getLogger(RootResource.class);
    private static final String QUERY_SYSTEM_PROTOTYPES
            = "SELECT DISTINCT ?prototype {"
            + "	?device a proto:Individual, ssn:System ;"
            + "    	proto:hasPrototype ?prototype ."
            + "}";
    private static final String QUERY_SENSOR_PROTOTYPES
            = "SELECT DISTINCT ?prototype {"
            + " ?device a proto:Individual, ssn:SensingDevice ;"
            + "         proto:hasPrototype ?prototype ."
            + "}";
    private static final String QUERY_COLLECTION_MEMBER
            = "SELECT ?uri {"
            + " <${COLLECTION_URI}> rdfs:range ?shape ."
            + " ?shape sh:property ?uri ."
            + " ?uri sh:predicate hydra:member ."
            + "}";
    private static final String QUERY_INDIVIDUAL_PROPERTIES
            = "SELECT DISTINCT ?prototype ?property {"
            + " ?device a proto:Individual ;"
            + "     proto:hasPrototype <${PROTOTYPE_URI}> ;"
            + "     proto:hasPrototype ?prototype ;"
            + "     ?property ?value ."
            + " FILTER(?property NOT IN (rdf:type, proto:hasPrototype))"
            + "}";
    private static final String VAR_PROTOTYPE = "prototype";
    private static final String VAR_PROPERTY = "property";
    private static final String VAR_URI = "uri";
    private static final String VAR_COLLECTION_URI = "${COLLECTION_URI}";
    private static final String VAR_PROTOTYPE_URI = "${PROTOTYPE_URI}";

    public RootResource() {
    }

    @Inject
    SPARQLQueryService query;

    @Context
    UriInfo uriInfo;

    @Inject
    ContextProvider contextProvider;

    @GET
    @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
    public String entrypoint() throws JsonLdError, IOException, URISyntaxException {
        URI root = uriInfo.getRequestUri();
        Model entrypoint = contextProvider.getRDFModel(ENTRYPOINT, root);
        Map<String, Object> frame = contextProvider.getFrame(ENTRYPOINT, root);

        Object entrypointObj = RDFUtils.toJsonLd(entrypoint);

        return JsonUtils.toString(JsonLdProcessor.frame(entrypointObj, frame, new JsonLdOptions()));
    }

    @GET
    @Produces({MediaType.TEXT_HTML, MediaType.TEXT_PLAIN})
    public Response index() {
        return Response.seeOther(URI.create("/index")).build();
    }

    @GET
    @Path("/doc")
    @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
    public void documentation(@Suspended final AsyncResponse response)
            throws JsonLdError, IOException {
        URI root = uriInfo.getRequestUri();
        Model apiDoc = contextProvider.getRDFModel(API_DOCUMENTATION, root);
        Map<String, Object> frame = contextProvider.getFrame(API_DOCUMENTATION, root);

        Observable<List<Resource>> systems = query.select(QUERY_SYSTEM_PROTOTYPES)
                .map((ResultSet rs) -> {
                    return defineResourceIndividual(apiDoc, root, "EntryPoint-Systems",
                            rs, SSN.System);
                });
        Observable<List<Resource>> sensors = query.select(QUERY_SENSOR_PROTOTYPES)
                .map((ResultSet rs) -> {
                    return defineResourceIndividual(apiDoc, root, "EntryPoint-Sensors",
                            rs, SSN.SensingDevice);
                });

        Observable.merge(systems, sensors).map((List<Resource> rs) -> {
            List<Observable<ResultSet>> obs = new ArrayList<>();
            rs.stream().forEach((prototype) -> {
                obs.add(query.select(QUERY_INDIVIDUAL_PROPERTIES
                        .replace(VAR_PROTOTYPE_URI, prototype.getURI())));
            });

            return Observable.merge(obs).toBlocking().toIterable();
        }).map((Iterable<ResultSet> iter) -> {
            iter.forEach((ResultSet rs) -> {
                while (rs.hasNext()) {
                    QuerySolution qs = rs.next();
                    Resource prototype = qs.getResource(VAR_PROTOTYPE);
                    Resource prototypeResource = ResourceUtils.createResourceFromClass(
                            root, prototype.getLocalName());
                    Property property = ResourceFactory.createProperty(
                            qs.getResource(VAR_PROPERTY).getURI());

                    apiDoc.add(prototypeResource, Hydra.supportedProperty, property);
                }
            });

            return apiDoc;
        }).lastOrDefault(apiDoc).map((__) -> {
            try {
                Object result = JsonLdProcessor.frame(
                        RDFUtils.toJsonLd(apiDoc), frame, new JsonLdOptions());

                return JsonUtils.toString(result);
            } catch (JsonLdError | IOException e) {
                throw Exceptions.propagate(e);
            }
        }).subscribe((o) -> {
            response.resume(o);
        }, (e) -> {
            logger.error(e.getMessage(), e);

            response.resume(e);
        });
    }

    private List<Resource> defineResourceIndividual(Model model, URI root, String collectionName,
            ResultSet rs, Resource... classes) {
        List<Resource> resultPrototypes = new ArrayList<>();
        final Resource apiDocResource = model.listResourcesWithProperty(
                RDF.type, Hydra.ApiDocumentation).next();
        final Resource collection = ResourceFactory.createResource(
                URIUtils.extractRootURL(root) + "/doc#" + collectionName);

        //Find the restriction on hydra:member of the given collection
        ResultSet results = query.select(model, QUERY_COLLECTION_MEMBER
                .replace(VAR_COLLECTION_URI, collection.getURI()));
        Resource restriction = null;
        if (results.hasNext()) {
            restriction = results.next().getResource(VAR_URI);
        }

        while (rs.hasNext()) {
            final Resource prototype = rs.next().getResource(VAR_PROTOTYPE);
            resultPrototypes.add(prototype);
            final Resource prototypeResource = ResourceUtils.createResourceFromClass(
                    root, prototype.getLocalName());

            //Define in hydra:supportedClass
            model.add(apiDocResource,
                    Hydra.supportedClass, prototypeResource);
            model.add(prototypeResource, RDF.type, Hydra.Class);
            model.add(prototypeResource, RDF.type, Proto.Individual);
            for (Resource clazz : classes) {
                model.add(prototypeResource, RDF.type, clazz);
            }
            model.add(prototypeResource, Proto.hasPrototype, prototype);

            //Define in the given collection
            if (restriction != null) {
                model.add(restriction, SHACL.clazz, prototypeResource);
            }
        }
        
        return resultPrototypes;
    }

}
