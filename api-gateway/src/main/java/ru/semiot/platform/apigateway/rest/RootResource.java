package ru.semiot.platform.apigateway.rest;

import com.github.jsonldjava.core.JsonLdError;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.apigateway.utils.JsonLdBuilder;
import ru.semiot.platform.apigateway.utils.JsonLdKeys;

@Path("/")
public class RootResource {

    private static final Logger logger = LoggerFactory.getLogger(RootResource.class);
    private static final Map<String, Object> CONTEXT = Stream.of(
            new AbstractMap.SimpleEntry<>("hydra", "http://www.w3.org/ns/hydra/core#")
    ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()));

    public RootResource(@Context UriInfo uriInfo) throws URISyntaxException {
        final String defaultVocabURL = 
                uriInfo.resolve(new URI("vocab#")).toASCIIString();
        CONTEXT.put("vocab", defaultVocabURL);
    }

    @Context
    UriInfo uriInfo;

    @GET
    @Produces("application/ld+json")
    public String entrypoint() throws JsonLdError, IOException, URISyntaxException {
        JsonLdBuilder builder = new JsonLdBuilder(CONTEXT)
                .add(JsonLdKeys.ID, uriInfo.getRequestUri().toASCIIString())
                .add(JsonLdKeys.TYPE, "vocab:EntryPoint")
                .add("vocab:EntryPoint/systems", uriInfo.resolve(new URI("systems/list")).toASCIIString());

        return builder.toCompactedString();
    }

    @GET
    @Path("/doc")
    @Produces("application/ld+json")
    public String documentation() throws JsonLdError, IOException {
        JsonLdBuilder builder = new JsonLdBuilder(CONTEXT)
                .add(JsonLdKeys.ID, uriInfo.getRequestUri().toASCIIString())
                .add(JsonLdKeys.TYPE, "hydra:ApiDocumentation")
                .add("hydra:entrypoint", uriInfo.getBaseUri().toASCIIString());

        return builder.toCompactedString();
    }
    
    @GET
    @Path("/vocab")
    @Produces("application/ld+json")
    public String vocabulary() throws IOException {
        final String context = IOUtils.toString(
                this.getClass().getResourceAsStream("/ru/semiot/platform/apigateway/context.jsonld"));
        
        return context.replace(
                "${VOCABULARY_URL}", 
                uriInfo.getBaseUriBuilder().path("vocab").build().toASCIIString());
    }

}
