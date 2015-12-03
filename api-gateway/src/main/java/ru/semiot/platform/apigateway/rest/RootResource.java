package ru.semiot.platform.apigateway.rest;

import com.github.jsonldjava.core.JsonLdError;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.apigateway.JsonLdContextProviderService;
import ru.semiot.platform.apigateway.utils.JsonLdBuilder;
import ru.semiot.platform.apigateway.utils.JsonLdKeys;

@Path("/")
@Stateless
public class RootResource {

    private static final Logger logger = LoggerFactory.getLogger(RootResource.class);

    public RootResource() {
    }

    @Context
    UriInfo uriInfo;
    
    @Inject
    JsonLdContextProviderService contextProvider;

    @GET
    @Produces(MediaType.APPLICATION_LD_JSON)
    public String entrypoint() throws JsonLdError, IOException, URISyntaxException {
        Map<String, Object> context = contextProvider.getContextAsJsonLd(
                JsonLdContextProviderService.ENTRYPOINT_CONTEXT, 
                uriInfo.getRequestUri());
        
        JsonLdBuilder builder = new JsonLdBuilder(context)
                .add(JsonLdKeys.ID, uriInfo.getRequestUri().toASCIIString())
                .add(JsonLdKeys.TYPE, "vocab:EntryPoint")
                .add("vocab:EntryPoint/systems", 
                        uriInfo.resolve(new URI("systems")).toASCIIString())
                .add("vocab:EntryPoint/sensors", 
                        uriInfo.resolve(new URI("sensors")).toASCIIString());

        return builder.toCompactedString();
    }

    @GET
    @Path("/doc")
    @Produces(MediaType.APPLICATION_LD_JSON)
    public String documentation() throws JsonLdError, IOException {
        return contextProvider.getContextAsString(
                JsonLdContextProviderService.API_DOCUMENTATION_CONTEXT, 
                uriInfo.getRequestUri());
    }

}
