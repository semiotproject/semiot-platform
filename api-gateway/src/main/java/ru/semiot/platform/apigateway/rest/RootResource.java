package ru.semiot.platform.apigateway.rest;

import com.github.jsonldjava.core.JsonLdError;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import ru.semiot.platform.apigateway.JsonLdContextProviderService;
import ru.semiot.platform.apigateway.SPARQLQueryService;
import ru.semiot.platform.apigateway.utils.Hydra;
import ru.semiot.platform.apigateway.utils.JsonLdBuilder;
import ru.semiot.platform.apigateway.utils.JsonLdKeys;
import ru.semiot.platform.apigateway.utils.MapBuilder;

@Path("/")
@Stateless
public class RootResource {

    private static final Logger logger = LoggerFactory.getLogger(RootResource.class);
    private static final String QUERY_PROTOTYPES
            = "SELECT DISTINCT ?prototype {"
            + "	?device a proto:Individual, ssn:System ;"
            + "    	proto:hasPrototype ?prototype ."
            + "}";
    private static final String VAR_PROTOTYPE = "prototype";

    public RootResource() {
    }

    @Inject
    SPARQLQueryService query;

    @Context
    UriInfo uriInfo;

    @Inject
    JsonLdContextProviderService contextProvider;

    @GET
    @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
    public String entrypoint() throws JsonLdError, IOException, URISyntaxException {
        Map<String, Object> context = contextProvider.getContextAsJsonLd(
                JsonLdContextProviderService.ENTRYPOINT_CONTEXT,
                uriInfo.getRequestUri());

        JsonLdBuilder builder = new JsonLdBuilder()
                .context(context)
                .append(JsonLdKeys.ID, uriInfo.getRequestUri().toASCIIString())
                .append(JsonLdKeys.TYPE, "vocab:EntryPoint")
                .append("vocab:EntryPoint/systems",
                        uriInfo.resolve(new URI("systems")).toASCIIString())
                .append("vocab:EntryPoint/sensors",
                        uriInfo.resolve(new URI("sensors")).toASCIIString());

        return builder.toCompactedString();
    }

    @GET
    @Produces({MediaType.TEXT_HTML, MediaType.TEXT_PLAIN})
    public Response index() {
        return Response.seeOther(URI.create("/index.html")).build();
    }

    @GET
    @Path("/doc")
    @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
    public void documentation(@Suspended final AsyncResponse response) throws JsonLdError, IOException {
        //TODO: Add declaration of prototypes from TS

        Map<String, Object> apiDoc = contextProvider.getContextAsJsonLd(
                JsonLdContextProviderService.API_DOCUMENTATION_CONTEXT,
                uriInfo.getRequestUri());

        JsonLdBuilder builder = new JsonLdBuilder()
                .context(apiDoc)
                .append(apiDoc);

        query.select(QUERY_PROTOTYPES).subscribe((ResultSet r) -> {
            try {
                while (r.hasNext()) {
                    final Resource prototype = r.next().getResource(VAR_PROTOTYPE);
                    builder.append(Hydra.supportedClass,
                            MapBuilder.newMap()
                            .put(JsonLdKeys.ID, "vocab:" + prototype.getLocalName() + "Resource")
                            .put(JsonLdKeys.TYPE, Hydra.Class, "proto:Individual")
                            .put("proto:hasPrototype", prototype.getURI())
                            .build());
                }

                response.resume(builder.toCompactedString());
            } catch (JsonLdError | IOException ex) {
                response.resume(ex);
            }
        }, (e) -> {
            logger.warn(e.getMessage(), e);

            response.resume(e);
        });
    }

}
