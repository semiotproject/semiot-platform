package ru.semiot.platform.apigateway.rest;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.utils.JsonUtils;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.commons.restapi.MediaType;
import ru.semiot.platform.apigateway.ServerConfig;
import ru.semiot.platform.apigateway.beans.TSDBQueryService;
import ru.semiot.platform.apigateway.beans.impl.ContextProvider;
import ru.semiot.platform.apigateway.beans.impl.SPARQLQueryService;
import ru.semiot.platform.apigateway.utils.MapBuilder;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import ru.semiot.platform.apigateway.utils.URIUtils;
import rx.exceptions.Exceptions;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static ru.semiot.commons.restapi.AsyncResponseHelper.resume;
import static ru.semiot.commons.restapi.AsyncResponseHelper.resumeOnError;

@Path("/systems/{system_id}/observations")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_LD_JSON})
public class SystemObservationsResource {

    private static final ServerConfig config = ConfigFactory.create(ServerConfig.class);

    @Inject
    private SPARQLQueryService sparqlQuery;
    @Inject
    private TSDBQueryService tsdbQuery;
    @Inject
    private ContextProvider contextProvider;
    @Context
    UriInfo uriInfo;

    @GET
    public void observations(@Suspended final AsyncResponse response,
                             @PathParam("system_id") String systemId,
                             @QueryParam("start") String start,
                             @QueryParam("end") String end)
            throws IOException {
        if (Strings.isNullOrEmpty(systemId)) {
            response.resume(Response.status(Response.Status.NOT_FOUND).build());
        }

        URI root = uriInfo.getRequestUri();
        Map params = MapBuilder.newMap()
                .put(ContextProvider.VAR_ROOT_URL, URIUtils.extractRootURL(root))
                .put(ContextProvider.VAR_WAMP_URL, config.wampUri())
                .put(ContextProvider.VAR_SYSTEM_ID, systemId)
                .build();

        if (Strings.isNullOrEmpty(start)) {
            tsdbQuery.queryTimeOfLatestBySystemId(systemId).subscribe((result) -> {
                if (result != null) {
                    response.resume(Response.seeOther(UriBuilder.fromUri(root)
                            .queryParam("start", result)
                            .build())
                            .build());
                } else {
                    try {
                        Map<String, Object> frame = contextProvider.getFrame(
                                ContextProvider.SYSTEM_OBSERVATIONS_COLLECTION, root);
                        params.put(ContextProvider.VAR_QUERY_PARAMS, "?noparams");
                        Model model = contextProvider.getRDFModel(
                                ContextProvider.SYSTEM_OBSERVATIONS_COLLECTION, params);
                        Resource view = RDFUtils.subjectWithProperty(
                                model, RDF.type, Hydra.PartialCollectionView);
                        model.removeAll(view, null, null);

                        response.resume(JsonUtils.toPrettyString(
                                RDFUtils.toJsonLdCompact(model, frame)));
                    } catch (Throwable ex) {
                        response.resume(ex);
                    }
                }
            }, resumeOnError(response));
        } else {
            Map<String, Object> frame = contextProvider.getFrame(
                    ContextProvider.SYSTEM_OBSERVATIONS_PARTIAL_COLLECTION, root);

            String queryParams = "?start=" + start;
            if (!Strings.isNullOrEmpty(end)) {
                queryParams += "&end=" + end;
            }
            params.put(ContextProvider.VAR_QUERY_PARAMS, queryParams);

            Model model = contextProvider.getRDFModel(
                    ContextProvider.SYSTEM_OBSERVATIONS_COLLECTION, params);
            tsdbQuery.queryBySystemId(systemId, start, end).map((result) -> {
                model.add(result);
                Resource collection = model.listSubjectsWithProperty(
                        RDF.type, Hydra.PartialCollectionView).next();
                ResIterator iter = model.listSubjectsWithProperty(RDF.type, SSN.Observaton);
                while (iter.hasNext()) {
                    Resource obs = iter.next();
                    model.add(collection, Hydra.member, obs);
                }
                try {
                    return JsonUtils.toPrettyString(
                            RDFUtils.toJsonLdCompact(model, frame));
                } catch (JsonLdError | IOException ex) {
                    throw Exceptions.propagate(ex);
                }
            }).map((json) -> Response.ok(json).build())
                    .subscribe(resume(response));
        }
    }
}
