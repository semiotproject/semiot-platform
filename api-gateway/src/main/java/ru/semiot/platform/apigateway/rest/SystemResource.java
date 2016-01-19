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
import java.net.URISyntaxException;
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
import ru.semiot.platform.apigateway.JsonLdContextProviderService;
import ru.semiot.platform.apigateway.SPARQLQueryService;
import ru.semiot.platform.apigateway.utils.JsonLdBuilder;
import ru.semiot.platform.apigateway.utils.JsonLdKeys;

@Path("/systems")
@Stateless
public class SystemResource {

    private static final Logger logger = LoggerFactory.getLogger(SystemResource.class);
    private static final String QUERY_GET_ALL_SYSTEMS
            = "SELECT DISTINCT ?uri ?id {"
            + "?uri a ssn:System, proto:Individual ;"
            + "dcterms:identifier ?id ."
            + "}";

    private static final String QUERY_DESCRIBE_SYSTEM
            = "DESCRIBE ?system_uri ?subsystem_uri {"
            + "	?system_uri dcterms:identifier \"${SYSTEM_ID}\"^^xsd:string ;"
            + "    ssn:hasSubSystem ?subsystem_uri ."
            + "}";

    public SystemResource() {
    }

    @Inject
    SPARQLQueryService query;

    @Inject
    JsonLdContextProviderService contextProvider;

    @Context
    private UriInfo uriInfo;

    @GET
    @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
    public void listSystems(@Suspended final AsyncResponse response)
            throws JsonLdError, IOException {
        final Map<String, Object> context = contextProvider.getContextAsJsonLd(
                JsonLdContextProviderService.ENTRYPOINT_CONTEXT,
                uriInfo.getRequestUri());

        final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
        final String requstUri = uriInfo.getRequestUri().toASCIIString();

        query.select(QUERY_GET_ALL_SYSTEMS).subscribe((ResultSet r) -> {
            JsonLdBuilder builder = new JsonLdBuilder()
                    .context(context)
                    .append(JsonLdKeys.ID, requstUri)
                    .append(JsonLdKeys.TYPE, "vocab:SystemCollection");

            while (r.hasNext()) {
                final UriBuilder ub = uriBuilder.clone();

                final QuerySolution qs = r.next();
                final String uri = ub
                        .path("systems/{a}")
                        .buildFromEncoded(qs.getLiteral("id").getString()).toASCIIString();

                builder.append("hydra:member", new HashMap<String, Object>() {
                    {
                        {
                            put(JsonLdKeys.ID, uri);
                            put(JsonLdKeys.TYPE, "ssn:System");
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
    @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
    public void getSystem(
            @Suspended final AsyncResponse response,
            @PathParam("id") String id) throws URISyntaxException, IOException {
        final Map<String, Object> frame = contextProvider.getContextAsJsonLd(
                JsonLdContextProviderService.SYSTEM_FRAME, uriInfo.getRequestUri());

        query.describe(QUERY_DESCRIBE_SYSTEM.replace("${SYSTEM_ID}", id)).subscribe((model) -> {
            StringWriter sw = new StringWriter();
            model.write(sw, "N-TRIPLE");

            try {
                Object b = JsonLdProcessor.fromRDF(
                        sw.toString(), new TurtleRDFParser());

                Map<String, Object> system = JsonLdProcessor.frame(
                        b, frame, new JsonLdOptions());

                response.resume(JsonUtils.toString(JsonLdProcessor.compact(
                        system, frame, new JsonLdOptions())));
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
