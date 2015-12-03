package ru.semiot.platform.apigateway.rest;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.impl.TurtleRDFParser;
import com.github.jsonldjava.utils.JsonUtils;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
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
            = "DESCRIBE ?system_uri {"
            + "?system_uri dcterms:identifier \"${SYSTEM_ID}\"^^xsd:string ."
            + "}";

    private static volatile Map<String, Object> context;

    public SystemResource() {
    }

    @Inject
    SPARQLQueryService query;

    @Context
    private UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_LD_JSON)
    public void listSystems(@Suspended final AsyncResponse response)
            throws JsonLdError, IOException {
        final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
        final String requstUri = uriInfo.getRequestUri().toASCIIString();

        query.select(QUERY_GET_ALL_SYSTEMS).subscribe((ResultSet r) -> {
            JsonLdBuilder builder = new JsonLdBuilder(context)
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
    @Produces(MediaType.APPLICATION_LD_JSON)
    public void getSystem(
            @Suspended final AsyncResponse response,
            @PathParam("id") String id) throws URISyntaxException {
        final String requestUri = uriInfo.getRequestUri().toASCIIString();
        final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        extendContext(uriInfo, context);

        query.describe(QUERY_DESCRIBE_SYSTEM.replace("${SYSTEM_ID}", id)).subscribe((model) -> {
            JsonLdBuilder builder = new JsonLdBuilder(context)
                    .add(JsonLdKeys.ID, requestUri)
                    .add(JsonLdKeys.TYPE, "ssn:System")
                    .add("vocab:observations", uriBuilder.scheme("ws")
                            .replacePath("/ws/observations/systems/{a}")
                            .build(id));

            StringWriter sw = new StringWriter();
            model.write(sw, "N-TRIPLE");

            try {
                Map<String, Object> frame = new JsonLdBuilder(context)
                        .add(JsonLdKeys.TYPE, new ArrayList<Object>() {
                            {
                                {
                                    add("ssn:System");
                                }
                            }
                        }).toJsonLdObject();

                Object b = JsonLdProcessor.fromRDF(sw.toString(), new TurtleRDFParser());

                Map<String, Object> o = JsonLdProcessor.frame(
                        b, frame, new JsonLdOptions());

                response.resume(JsonUtils.toString(JsonLdProcessor.compact(o, context, new JsonLdOptions())));

//                response.resume(JsonLdProcessor.flatten(b, CONTEXT, new JsonLdOptions()));
            } catch (JsonLdError | IOException ex) {
                logger.warn(ex.getMessage(), ex);

                response.resume(ex);
            }
        }, (e) -> {
            logger.warn(e.getMessage(), e);

            response.resume(e);
        });
    }

    private void extendContext(UriInfo uriInfo, Map<String, Object> context)
            throws URISyntaxException {
        final String defaultVocabURL
                = uriInfo.resolve(new URI("vocab#")).toASCIIString();
        final String ssnURL
                = uriInfo.resolve(new URI("ssn#")).toASCIIString();
        context.put("vocab", defaultVocabURL);
        context.put("ssncontext", ssnURL);
    }
}
