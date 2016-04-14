package ru.semiot.platform.apigateway.rest;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.utils.JsonUtils;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.commons.namespaces.SEMIOT;
import ru.semiot.commons.restapi.MediaType;
import ru.semiot.platform.apigateway.ServerConfig;
import ru.semiot.platform.apigateway.beans.TSDBQueryService;
import ru.semiot.platform.apigateway.beans.impl.ContextProvider;
import ru.semiot.platform.apigateway.beans.impl.DeviceProxyService;
import ru.semiot.platform.apigateway.utils.MapBuilder;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import ru.semiot.platform.apigateway.utils.URIUtils;
import rx.exceptions.Exceptions;

import javax.ejb.Stateless;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static ru.semiot.commons.restapi.AsyncResponseHelper.resume;
import static ru.semiot.commons.restapi.AsyncResponseHelper.resumeOnError;

@Path("/systems/{system_id}/actuations")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_LD_JSON})
@Stateless
public class SystemActuationsResource {

    private static final Logger logger = LoggerFactory.getLogger(SystemActuationsResource.class);
    private static final ServerConfig config = ConfigFactory.create(ServerConfig.class);

    @Inject
    ContextProvider contextProvider;
    @Inject
    TSDBQueryService tsdb;
    @Inject
    DeviceProxyService dps;
    @Context
    UriInfo uriInfo;

    @GET
    public void actuations(@Suspended AsyncResponse response,
                           @PathParam("system_id") String systemId,
                           @QueryParam("start") ZonedDateTime start,
                           @QueryParam("end") ZonedDateTime end) {
        URI root = uriInfo.getRequestUri();
        Map params = MapBuilder.newMap()
                .put(ContextProvider.VAR_ROOT_URL, URIUtils.extractRootURL(root))
                .put(ContextProvider.VAR_WAMP_URL, config.wampUri())
                .put(ContextProvider.VAR_SYSTEM_ID, systemId)
                .build();
        if (Strings.isNullOrEmpty(systemId)) {
            response.resume(Response.status(Response.Status.NOT_FOUND).build());
        }

        if (start == null) {
            tsdb.queryDateTimeOfLatestActuation(systemId).subscribe((dateTime) -> {
                if (dateTime != null) {
                    response.resume(Response.seeOther(UriBuilder.fromUri(root)
                            .queryParam("start", dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                            .build())
                            .build());
                } else {
                    try {
                        Map<String, Object> frame = contextProvider.getFrame(
                                ContextProvider.SYSTEM_ACTUATIONS_COLLECTION, root);
                        params.put(ContextProvider.VAR_QUERY_PARAMS, "?noparams");
                        Model model = contextProvider.getRDFModel(
                                ContextProvider.SYSTEM_ACTUATIONS_COLLECTION, params);
                        Resource view = RDFUtils.subjectWithProperty(
                                model, RDF.type, Hydra.PartialCollectionView);
                        model.removeAll(view, null, null);

                        response.resume(JsonUtils.toPrettyString(
                                RDFUtils.toJsonLdCompact(model, frame)));
                    } catch (Throwable ex) {
                        resumeOnError(response, ex);
                    }
                }
            }, resumeOnError(response));
        } else {
            String queryParams = "?start=" + start;
            if (end != null) {
                queryParams += "&end=" + end;
            }
            params.put(ContextProvider.VAR_QUERY_PARAMS, queryParams);

            Model model = contextProvider.getRDFModel(
                    ContextProvider.SYSTEM_ACTUATIONS_COLLECTION, params);
            tsdb.queryActuationsByRange(systemId, start, end).subscribe((result) -> {
                try {
                    Map<String, Object> frame = contextProvider.getFrame(
                            ContextProvider.SYSTEM_ACTUATIONS_PARTIAL_COLLECTION, root);
                    model.add(result);
                    Resource collection = model.listSubjectsWithProperty(
                            RDF.type, Hydra.PartialCollectionView).next();
                    ResIterator iter = model.listSubjectsWithProperty(RDF.type, SEMIOT.Actuation);
                    while (iter.hasNext()) {
                        Resource obs = iter.next();
                        model.add(collection, Hydra.member, obs);
                    }

                    response.resume(JsonUtils.toPrettyString(
                            RDFUtils.toJsonLdCompact(model, frame)));
                } catch (Throwable e) {
                    resumeOnError(response, e);
                }
            }, resumeOnError(response));
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_LD_JSON, MediaType.TEXT_TURTLE})
    public void executeCommand(@Suspended AsyncResponse response,
                               @PathParam("system_id") String systemId,
                               Model command) {
        try {
            URI root = uriInfo.getRequestUri();
            Map<String, Object> frame = contextProvider.getFrame(ContextProvider.ACTUATION, root);

            if (Strings.isNullOrEmpty(systemId)) {
                response.resume(Response.status(Response.Status.NOT_FOUND).build());
            }

            if (command == null || command.isEmpty()) {
                response.resume(Response.status(Response.Status.BAD_REQUEST).build());
            } else {
                dps.executeCommand(systemId, command).map((actuation) -> {
                    try {
                        return Response.ok(RDFUtils.toJsonLdCompact(actuation, frame)).build();
                    } catch (JsonLdError | IOException ex) {
                        throw Exceptions.propagate(ex);
                    }
                }).subscribe(resume(response));
            }
        } catch (Throwable ex) {
            resumeOnError(response, ex);
        }
    }
}
