package ru.semiot.platform.apigateway.beans.impl;

import org.aeonbits.owner.ConfigFactory;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.glassfish.jersey.client.rx.Rx;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.DUL;
import ru.semiot.platform.apigateway.ServerConfig;
import ru.semiot.platform.apigateway.beans.TSDBQueryService;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import rx.Observable;
import rx.exceptions.Exceptions;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.inject.Default;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Singleton
@Default
public class TSDBQueryServiceImpl implements TSDBQueryService {

    private static final Logger logger = LoggerFactory.getLogger(TSDBQueryServiceImpl.class);
    private static final ServerConfig config = ConfigFactory.create(ServerConfig.class);

    @Resource
    ManagedExecutorService mes;

    @Override
    public Observable<String> queryTimeOfLatestBySensorUri(String systemId, String sensorUri) {
        return null;
    }

    @Override
    public Observable<String> queryTimeOfLatestBySystemId(String systemId) {
        return Observable.just(null);
    }

    @Override
    public Observable<Model> queryLatestBySystemId(String systemId) {
        return null;
    }

    @Override
    public Observable<Model> queryBySensorUri(String systemId, String sensorUri, String start, String end) {
        return null;
    }

    @Override
    public Observable<Model> queryBySystemId(String systemId, String start, String end) {
        return null;
    }

    @Override
    public Observable<ZonedDateTime> queryDateTimeOfLatestActuation(String systemId) {
        URI uri = UriBuilder.fromPath(config.tsdbEndpoint())
                .path(config.tsdbActuatorsLatestPath())
                .queryParam("system_id", systemId).build();

        logger.debug("queryDateTimeOfLatestActuation: URL {}", uri);

        Observable<Response> result = Rx
                .newClient(RxObservableInvoker.class, mes)
                .target(uri).request().rx().get();

        return result.map((response -> {
            if (response.getStatus() == 200) {
                Model model = RDFUtils.toModel(response.readEntity(String.class),
                        RDFLanguages.JSONLD);
                if (model.isEmpty()) {
                    return null;
                } else {
                    Literal dateTime = (Literal) model.listObjectsOfProperty(
                            DUL.hasEventTime).next();

                    return ZonedDateTime.parse(dateTime.getLexicalForm(),
                            DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                }
            } else {
                logger.debug("None 200 nor 404");
                return null;
//                throw Exceptions.propagate(new WebApplicationException(Response
//                        .status(response.getStatus())
//                        .entity(response.readEntity(String.class))
//                        .build()));
            }
        }));
    }

    public Observable<Model> queryActuationsByRange(String systemId,
                                                    ZonedDateTime start,
                                                    ZonedDateTime end) {
        UriBuilder uriBuilder = UriBuilder
                .fromPath(config.tsdbEndpoint())
                .path(config.tsdbActuatorsPath())
                .queryParam("system_id", systemId)
                .queryParam("start", start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        if (end != null) {
            uriBuilder.queryParam("end", end.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
        URI uri = uriBuilder.build();

        logger.debug("queryActuationsByRange: URL {}", uri);

        Observable<Response> result = Rx
                .newClient(RxObservableInvoker.class, mes)
                .target(uri).request().rx().get();

        return result.map((response -> {
            if (response.getStatus() == 200) {
                return RDFUtils.toModel(response.readEntity(String.class),
                        RDFLanguages.JSONLD);
            } else {
                logger.debug("None 200 nor 404");
                return null;
//                throw Exceptions.propagate(new WebApplicationException(Response
//                        .status(response.getStatus())
//                        .entity(response.readEntity(String.class))
//                        .build()));
            }
        }));
    }
}
