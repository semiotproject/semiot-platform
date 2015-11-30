package ru.semiot.platform.apigateway.rest;

import com.github.jsonldjava.core.JsonLdError;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.apigateway.SPARQLQueryService;
import ru.semiot.platform.apigateway.utils.JsonLdBuilder;
import ru.semiot.platform.apigateway.utils.JsonLdKeys;

@Path("/systems")
@Stateless
public class SystemResource {

    private static final Logger logger = LoggerFactory.getLogger(SystemResource.class);
    private static final String QUERY_GET_ALL_SYSTEMS
            = "SELECT DISTINCT ?uri ?label ?id {"
            + "?uri a ssn:System ;"
            + "rdfs:label ?label ;"
            + "dcterms:identifier ?id ."
            + "}";
    private static final String QUERY_DESCRIBE_SYSTEM
            = "SELECT * {"
            + "?uri dcterms:identifier \"${SYSTEM_ID}\"^^xsd:string ."
            + "?uri ?p ?o ."
            + "FILTER (isBlank(?o) = False)"
            + "}";
    private static final Map<String, Object> CONTEXT = Stream.of(
            new AbstractMap.SimpleEntry<>("hydra", "http://www.w3.org/ns/hydra/core#"),
            new AbstractMap.SimpleEntry<>("ssn", "http://purl.oclc.org/NET/ssnx/ssn#"),
            new AbstractMap.SimpleEntry<>("rdfs", "http://www.w3.org/2000/01/rdf-schema#"),
            new AbstractMap.SimpleEntry<>("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
            new AbstractMap.SimpleEntry<>("dul", "http://www.loa-cnr.it/ontologies/DUL.owl#")
    ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()));

    public SystemResource() {
    }

    public SystemResource(@Context UriInfo uriInfo) throws URISyntaxException {
        final String defaultVocabURL
                = uriInfo.resolve(new URI("vocab#")).toASCIIString();
        CONTEXT.put("vocab", defaultVocabURL);
    }

    @Inject
    SPARQLQueryService query;

    @Context
    private UriInfo context;

    @GET
    @Produces("application/ld+json")
    public void listSystems(@Suspended final AsyncResponse response)
            throws JsonLdError, IOException {
        final UriBuilder uriBuilder = context.getBaseUriBuilder();
        final String requstUri = context.getRequestUri().toASCIIString();

        query.select(QUERY_GET_ALL_SYSTEMS).subscribe((ResultSet r) -> {
            JsonLdBuilder builder = new JsonLdBuilder(CONTEXT)
                    .add(JsonLdKeys.ID, requstUri)
                    .add(JsonLdKeys.TYPE, "vocab:SystemCollection");

            while (r.hasNext()) {
                final UriBuilder ub = uriBuilder.clone();
                
                final QuerySolution qs = r.next();
                final String uri = ub
                        .path("systems/{a}")
                        .buildFromEncoded(qs.getLiteral("id").getString()).toASCIIString();
                final String label = qs.getLiteral("label").getString();

                builder.add("hydra:member", new HashMap<String, Object>() {
                    {
                        {
                            put(JsonLdKeys.ID, uri);
                            put(JsonLdKeys.TYPE, "ssn:System");
                            put("rdfs:label", label);
                        }
                    }
                });
            }

            try {
                response.resume(builder.toCompactedString());
            } catch (JsonLdError | IOException ex) {
                response.resume(ex);
            }

        }, (e) -> {
            logger.warn(e.getMessage(), e);

            response.resume(e);
        });
    }

    @GET
    @Path("{id}")
    @Produces("application/ld+json")
    public void getSystem(
            @Suspended final AsyncResponse response,
            @PathParam("id") String id) {
        final String requestUri = context.getRequestUri().toASCIIString();
        final UriBuilder uriBuilder = context.getBaseUriBuilder();

        query.select(QUERY_DESCRIBE_SYSTEM.replace("${SYSTEM_ID}", id)).subscribe((r) -> {
            JsonLdBuilder builder = new JsonLdBuilder(CONTEXT)
                    .add(JsonLdKeys.ID, requestUri)
                    .add(JsonLdKeys.TYPE, "ssn:System")
                    .add("vocab:observations", uriBuilder.scheme("ws")
                            .replacePath("/ws/observations/systems/{a}")
                            .build(id));

            while (r.hasNext()) {
                final QuerySolution qs = r.next();
                builder.add(qs.getResource("p").getURI(), qs.get("o").toString());
            }

            try {
                response.resume(builder.toCompactedString());
            } catch (JsonLdError | IOException ex) {
                logger.warn(ex.getMessage(), ex);

                response.resume(ex);
            }
        }, (e) -> {
            logger.warn(e.getMessage(), e);

            response.resume(e);
        });
    }
}
