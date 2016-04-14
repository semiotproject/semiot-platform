package ru.semiot.platform.apigateway.rest;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.utils.JsonUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.Proto;
import ru.semiot.commons.restapi.MediaType;
import ru.semiot.platform.apigateway.beans.impl.ContextProvider;
import ru.semiot.platform.apigateway.beans.impl.SPARQLQueryService;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import rx.exceptions.Exceptions;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static ru.semiot.commons.restapi.AsyncResponseHelper.*;

@Path("/systems/{system_id}/subsystems")
@Stateless
public class SubSystemResource {

    private static final Logger logger = LoggerFactory.getLogger(SubSystemResource.class);
    private static final String QUERY_DESCRIBE_SUBSYSTEM
            = "CONSTRUCT {"
            + "	?subsystem ?p ?o ."
            + " ?o ?o_p ?o_o ."
            + "} WHERE {"
            + " ?subsystem ?p ?o ;"
            + "  dcterms:identifier \"${SUBSYSTEM_ID}\"^^xsd:string ."
            + " OPTIONAL {"
            + "  ?o ?o_p ?o_o ."
            + "  FILTER isBlank(?o)"
            + " }"
            + "}";

    @Inject
    private ContextProvider contextProvider;
    @Inject
    private SPARQLQueryService sparqlQuery;
    @Context
    private UriInfo uriInfo;

    @GET
    @Path("/{subsystem_id}")
    @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
    public void getSubsystem(@Suspended final AsyncResponse response,
                             @PathParam("system_id") String systemId,
                             @PathParam("subsystem_id") String subsystemId)
            throws IOException {
        URI root = uriInfo.getRequestUri();
        Model model = contextProvider.getRDFModel(
                ContextProvider.SUBSYSTEM_SINGLE, root);
        Map<String, Object> frame = contextProvider.getFrame(
                ContextProvider.SUBSYSTEM_SINGLE, root);

        sparqlQuery.describe(QUERY_DESCRIBE_SUBSYSTEM.replace("${SUBSYSTEM_ID}", subsystemId))
                .map((Model result) -> {
                    model.add(result);
                    try {
                        Literal identifier = ResourceFactory.createTypedLiteral(
                                subsystemId, XSDDatatype.XSDstring);
                        Resource subsystem = model.listResourcesWithProperty(
                                DCTerms.identifier, identifier).next();
                        Resource prototype = model.listObjectsOfProperty(
                                subsystem, Proto.hasPrototype).next().asResource();
                        Resource prototypeResource = ResourceUtils
                                .createResourceFromClass(root, prototype.getLocalName());
                        model.add(subsystem, RDF.type, prototypeResource);

                        return JsonUtils.toPrettyString(
                                RDFUtils.toJsonLdCompact(model, frame));
                    } catch (JsonLdError | IOException ex) {
                        throw Exceptions.propagate(ex);
                    }
                }).subscribe(resume(response));
    }
}
