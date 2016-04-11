package ru.semiot.platform.apigateway.beans.impl;

import java.io.StringReader;
import java.util.List;

import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.inject.Default;
import javax.json.JsonArray;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.aeonbits.owner.ConfigFactory;
import org.apache.http.HttpStatus;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.glassfish.jersey.client.rx.Rx;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.semiot.platform.apigateway.ServerConfig;
import ru.semiot.platform.apigateway.beans.TSDBQueryService;
import rx.Observable;

@Singleton
@Default
public class TSDBQueryServiceImpl implements TSDBQueryService {

    private static final Logger logger = LoggerFactory
            .getLogger(TSDBQueryServiceImpl.class);
    private static final ServerConfig config = ConfigFactory
            .create(ServerConfig.class);

    private static final String QUERY_OBSERVATIONS = "/observations";
    private static final String QUERY_OBSERVATIONS_LATEST = "/observations/latest";
    private static final String QUERY_TIME_OBSERVATIONS_LATEST = "/observations/latest/time";
    private static final String QUERY_REMOVE = "/observations/remove";
    private static final String PARAM_SYSTEM_ID = "system_id";
    private static final String PARAM_SENSOR_ID = "sensor_id";
    private static final String PARAM_START = "start";
    private static final String PARAM_END = "end";

    @javax.annotation.Resource
    ManagedExecutorService mes;

    @Override
    public Observable<String> queryTimeOfLatestBySystemId(String systemId,
            List<String> sensorsId) {
        Observable<Response> get = Rx.newClient(RxObservableInvoker.class, mes)
                .target(UriBuilder.fromPath(config.tsdbEndpoint())
                        .path(QUERY_TIME_OBSERVATIONS_LATEST)
                        .queryParam(PARAM_SYSTEM_ID, systemId)
                        .queryParam(PARAM_SENSOR_ID, sensorsId.toArray())
                        .build())
                .request().rx().get();

        return get.map((response) -> {
            return response.readEntity(String.class);
        });
    }

    @Override
    public Observable<Model> queryLatestBySystemId(String systemId,
            List<String> sensorsId) {
        Observable<Response> get = Rx.newClient(RxObservableInvoker.class, mes)
                .target(UriBuilder.fromPath(config.tsdbEndpoint())
                        .path(QUERY_OBSERVATIONS_LATEST)
                        .queryParam(PARAM_SYSTEM_ID, systemId)
                        .queryParam(PARAM_SENSOR_ID, sensorsId.toArray())
                        .build())
                .request().rx().get();

        return responseToModel(get);
    }

    @Override
    public Observable<Model> queryBySystemId(String systemId,
            List<String> sensorsId, String start, String end) {
        if (Strings.isNullOrEmpty(start)) {
            throw new IllegalArgumentException();
        }

        UriBuilder uriBuilder = UriBuilder.fromPath(config.tsdbEndpoint())
                .path(QUERY_OBSERVATIONS).queryParam(PARAM_SYSTEM_ID, systemId)
                .queryParam(PARAM_SENSOR_ID, sensorsId.toArray())
                .queryParam(PARAM_START, start);

        if (!Strings.isNullOrEmpty(end)) {
            uriBuilder.queryParam(PARAM_END, end);
        }

        Observable<Response> get = Rx.newClient(RxObservableInvoker.class, mes)
                .target(uriBuilder.build()).request().rx().get();

        return responseToModel(get);
    }

    Observable<Model> responseToModel(Observable<Response> get) {
        return get.map((response) -> {
            Model model = ModelFactory.createDefaultModel();
            try {
                if (response.getStatus() == HttpStatus.SC_OK) {
                    StringReader reader = new StringReader(
                            response.readEntity(String.class));
                    model.read(reader, null, RDFLanguages.strLangJSONLD);
                }
            } catch (RiotException re) {
                logger.error(re.getMessage(), re);
            }
            return model;
        });
    }

    public Observable<Response> remove(JsonArray jsonArray) {
        return Rx.newClient(RxObservableInvoker.class, mes)
                .target(UriBuilder.fromPath(config.tsdbEndpoint())
                        .path(QUERY_REMOVE))
                .request().rx().post(Entity.entity(jsonArray.toString(),
                        MediaType.TEXT_PLAIN));
    }
}
