package ru.semiot.platform.apigateway.rest;

import static ru.semiot.commons.restapi.AsyncResponseHelper.resume;
import static ru.semiot.commons.restapi.AsyncResponseHelper.resumeOnError;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.utils.JsonUtils;

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

@Path("/systems/{system_id}/observations")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_LD_JSON})
public class SystemObservationsResource {

    private static final ServerConfig config = ConfigFactory
            .create(ServerConfig.class);

    private static final String QUERY_GET_SYSTEM_SENSORS = "SELECT DISTINCT ?sensor_id {"
            + " ?uri a ssn:System ;"
            + "     dcterms:identifier \"${SYSTEM_ID}\"^^xsd:string ;"
            + "     ssn:hasSubSystem ?sensor ."
            + " ?sensor dcterms:identifier ?sensor_id }";

    private static final String VAR_SENSOR_ID = "sensor_id";

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
            @QueryParam("sensor_id") List<String> listSensorId,
            @QueryParam("start") String start, @QueryParam("end") String end)
            throws IOException {
        if (Strings.isNullOrEmpty(systemId)) {
            response.resume(Response.status(Response.Status.NOT_FOUND).build());
        }

        URI root = uriInfo.getRequestUri();
        Map params = MapBuilder.newMap()
                .put(ContextProvider.VAR_ROOT_URL,
                        URIUtils.extractRootURL(root))
                .put(ContextProvider.VAR_WAMP_URL, config.wampUri())
                .put(ContextProvider.VAR_SYSTEM_ID, systemId).build();
        if (Strings.isNullOrEmpty(start)) {
            tsdbQuery
                    .queryTimeOfLatestBySystemId(systemId,
                            getListSensorsId(systemId, listSensorId))
                    .subscribe((result) -> {
                        if (StringUtils.isNotBlank(result)) {
                            response.resume(Response.seeOther(UriBuilder
                                    .fromUri(root).queryParam("start", result)
                                    .build()).build());
                        } else {
                            try {
                                Map<String, Object> frame = contextProvider
                                        .getFrame(
                                                ContextProvider.SYSTEM_OBSERVATIONS_COLLECTION,
                                                root);
                                params.put(ContextProvider.VAR_QUERY_PARAMS,
                                        "?noparams");
                                Model model = contextProvider.getRDFModel(
                                        ContextProvider.SYSTEM_OBSERVATIONS_COLLECTION,
                                        params);
                                Resource view = RDFUtils.subjectWithProperty(
                                        model, RDF.type,
                                        Hydra.PartialCollectionView);
                                model.remove(view, null, null);

                                response.resume(JsonUtils.toPrettyString(
                                        RDFUtils.toJsonLdCompact(model,
                                                frame)));
                            } catch (Throwable ex) {
                                response.resume(ex);
                            }
                        }
                    }, resumeOnError(response));
        } else {
            Map<String, Object> frame = contextProvider.getFrame(
                    ContextProvider.SYSTEM_OBSERVATIONS_PARTIAL_COLLECTION,
                    root);

            String queryParams = "?start=" + start;
            if (!Strings.isNullOrEmpty(end)) {
                queryParams += "&end=" + end;
            }
            params.put(ContextProvider.VAR_QUERY_PARAMS, queryParams);

            Model model = contextProvider.getRDFModel(
                    ContextProvider.SYSTEM_OBSERVATIONS_COLLECTION, params);

            tsdbQuery.queryBySystemId(systemId,
                    getListSensorsId(systemId, listSensorId), start, end)
                    .map((result) -> {
                        model.add(result);
                        Resource collection = model.listSubjectsWithProperty(
                                RDF.type, Hydra.PartialCollectionView).next();
                        ResIterator iter = model.listSubjectsWithProperty(
                                RDF.type, SSN.Observaton);
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
                    }).map((json) -> {
                        return Response.ok(json).build();
                    }).subscribe(resume(response));
        }
    }
    
    private List<String> getListSensorsId(String systemId, 
            List<String> sensorsId) {
        if (sensorsId == null || sensorsId.isEmpty()) {
            return rsToList(
                    sparqlQuery
                            .select(QUERY_GET_SYSTEM_SENSORS
                                    .replace("${SYSTEM_ID}", systemId))
                            .toBlocking().single());
        } else {
            return sensorsId;
        }
    }

    private List<String> rsToList(ResultSet rs) {
        List<String> list = new ArrayList<String>();
        while (rs.hasNext()) {
            QuerySolution qs = rs.next();
            list.add(qs.getLiteral(VAR_SENSOR_ID).getString());
        }
        return list;
    }

}
