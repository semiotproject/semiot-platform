package ru.semiot.platform.apigateway.rest;

import com.github.jsonldjava.core.JsonLdError;
import com.hp.hpl.jena.reasoner.rulesys.builtins.UriConcat;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import ru.semiot.platform.apigateway.utils.JsonLdBuilder;
import ru.semiot.platform.apigateway.utils.JsonLdKeys;

@Path("/systems")
public class SystemResource {

    private static final Map<String, Object> CONTEXT = Stream.of(
            new AbstractMap.SimpleEntry<>("hydra", "http://www.w3.org/ns/hydra/core#"),
            new AbstractMap.SimpleEntry<>("ssn", "http://purl.oclc.org/NET/ssnx/ssn#")
    ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()));

    public SystemResource(@Context UriInfo uriInfo) throws URISyntaxException {
        final String defaultVocabURL
                = uriInfo.resolve(new URI("vocab#")).toASCIIString();
        CONTEXT.put("vocab", defaultVocabURL);
    }

    @Context
    private UriInfo context;

    @GET
    @Path("/list")
    @Produces("application/ld+json")
    public String listSystems() throws JsonLdError, IOException {
        JsonLdBuilder builder = new JsonLdBuilder(CONTEXT)
                .add(JsonLdKeys.ID, context.getRequestUri().toASCIIString())
                .add(JsonLdKeys.TYPE, "vocab:SystemCollection")
                .array("hydra:member")
                .arrayAdd("hydra:member", new HashMap<String, Object>() {
                    {
                        {
                            put(JsonLdKeys.ID, "/system/1");
                            put(JsonLdKeys.TYPE, "ssn:System");
                        }
                    }
                })
                .arrayAdd("hydra:member", new HashMap<String, Object>() {
                    {
                        {
                            put(JsonLdKeys.ID, "/system/2");
                            put(JsonLdKeys.TYPE, "ssn:System");
                        }
                    }
                });

        return builder.toCompactedString();
    }

    @GET
    @Path("/single")
    @Produces("application/ld+json")
    public String getSystem(@QueryParam("uri") String uri) {
        return "[]";
    }
}
