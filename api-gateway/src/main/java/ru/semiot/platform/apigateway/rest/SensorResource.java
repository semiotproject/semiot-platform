package ru.semiot.platform.apigateway.rest;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.impl.TurtleRDFParser;
import com.github.jsonldjava.utils.JsonUtils;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
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
import ru.semiot.platform.apigateway.utils.URIUtils;

@Path("/sensors")
@Stateless
public class SensorResource {

    private static final Logger logger = LoggerFactory.getLogger(SensorResource.class);
    private static final String QUERY_GET_ALL_SENSORS
            = "SELECT DISTINCT ?uri ?label ?id {"
            + "?uri a ssn:SensingDevice ;"
            + "dcterms:identifier ?id ."
            + "OPTIONAL {?uri rdfs:label ?label .}"
            + "}";

    private static final String QUERY_DESCRIBE_SENSOR
            = "DESCRIBE ?uri {"
            + "	?uri dcterms:identifier \"${SENSOR_ID}\"^^xsd:string ."
            + "}";
    private static final String VAR_ID = "id";
    private static final String VAR_LABEL = "label";
    private static final Property DCTERMS_IDENTIFIER = 
            ResourceFactory.createProperty("http://purl.org/dc/terms/#identifier");
    private static final String VOCAB_OBSERVATIONS = "http://${HOST}/doc#observations";

    @Inject
    JsonLdContextProviderService contextProvider;

    @Inject
    SPARQLQueryService query;

    @Context
    private UriInfo uriInfo;

    public SensorResource() {
    }

    @GET
    @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
    public void listSensors(@Suspended final AsyncResponse response)
            throws IOException {
        final Map<String, Object> context = contextProvider.getContextAsJsonLd(
                JsonLdContextProviderService.ENTRYPOINT_CONTEXT,
                uriInfo.getRequestUri());

        final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
        final String requstUri = uriInfo.getRequestUri().toASCIIString();

        query.select(QUERY_GET_ALL_SENSORS).subscribe((ResultSet r) -> {
            JsonLdBuilder builder = new JsonLdBuilder(context)
                    .add(JsonLdKeys.ID, requstUri)
                    .add(JsonLdKeys.TYPE, "vocab:SensorCollection");

            while (r.hasNext()) {
                final UriBuilder ub = uriBuilder.clone();

                final QuerySolution qs = r.next();
                final String id = qs.getLiteral(VAR_ID).getString();
                final String uri = ub
                        .path("sensors/{a}")
                        .buildFromEncoded(id).toASCIIString();
                final String label;
                if (qs.contains(VAR_LABEL)) {
                    label = qs.getLiteral(VAR_LABEL).getString();
                } else {
                    label = null;
                }

                builder.add("hydra:member", new HashMap<String, Object>() {
                    {
                        {
                            put(JsonLdKeys.ID, uri);
                            put(JsonLdKeys.TYPE, "ssn:SensingDevice");

                            if (label != null) {
                                put("rdfs:label", label);
                            }
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
    public void getSensor(@Suspended final AsyncResponse response,
            @PathParam("id") String id) throws IOException {
        final URI requestUri = uriInfo.getRequestUri();
        final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
        final Map<String, Object> frame = contextProvider.getContextAsJsonLd(
                JsonLdContextProviderService.SENSOR_FRAME, uriInfo.getRequestUri());

        query.describe(QUERY_DESCRIBE_SENSOR.replace("${SENSOR_ID}", id)).subscribe((model) -> {
            model.listResourcesWithProperty(
                    DCTERMS_IDENTIFIER, 
                    ResourceFactory.createTypedLiteral(id, XSDDatatype.XSDstring))
                    .forEachRemaining((r) -> {
                        r.addProperty(
                                ResourceFactory.createProperty(
                                        VOCAB_OBSERVATIONS.replace("${HOST}", 
                                                URIUtils.extractHostName(requestUri))), 
                                uriBuilder
                                        .replacePath("/ws/sensors/{id}/observations")
                                        .build(id).toASCIIString());
                    });
            
            StringWriter sw = new StringWriter();
            model.write(sw, "N-TRIPLE");

            try {
                Object b = JsonLdProcessor.fromRDF(
                        sw.toString(), new TurtleRDFParser());

                Map<String, Object> sensor = JsonLdProcessor.frame(
                        b, frame, new JsonLdOptions());
                
                response.resume(JsonUtils.toString(JsonLdProcessor.compact(
                        sensor, frame, new JsonLdOptions())));
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
