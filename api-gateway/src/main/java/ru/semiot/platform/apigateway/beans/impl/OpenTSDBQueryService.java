package ru.semiot.platform.apigateway.beans.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.inject.Alternative;
import javax.json.JsonArray;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.aeonbits.owner.ConfigFactory;
import org.apache.http.HttpStatus;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.glassfish.jersey.client.rx.Rx;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.semiot.commons.namespaces.QUDT;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.platform.apigateway.ServerConfig;
import ru.semiot.platform.apigateway.beans.TSDBQueryService;
import ru.semiot.platform.apigateway.utils.JsonNavigator;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import rx.Observable;
import rx.exceptions.Exceptions;

@Singleton
@Alternative
public class OpenTSDBQueryService implements TSDBQueryService {

    private static final Logger logger = LoggerFactory
            .getLogger(OpenTSDBQueryService.class);
    private static final ServerConfig config = ConfigFactory
            .create(ServerConfig.class);
    private static final String SLASH = "/";
    private static final String PERCENT = "%";
    private static final String QUERY_ENDPOINT = "/api/query";
    private static final String QUERY_LAST_ENDPOINT = "/api/query/last";
    private static final String PARAM_START = "start";
    private static final String PARAM_END = "end";
    private static final String PARAM_METRICS = "m";
    private static final String PARAM_TIMESERIES = "timeseries";
    private static final String PARAM_RESOLVE = "resolve";

    @javax.annotation.Resource
    ManagedExecutorService mes;

    public OpenTSDBQueryService() {
    }

    public Observable<String> queryTimeOfLatestBySensorUri(String systemId,
            String sensorUri) {
        URI uri = null;
        try {
            uri = UriBuilder.fromPath(config.tsdbEndpoint())
                    .path(QUERY_LAST_ENDPOINT)
                    .queryParam(PARAM_TIMESERIES,
                            URLEncoder.encode(
                                    systemId + "{sensor_uri="
                                            + encode(sensorUri) + "}",
                                    StandardCharsets.UTF_8.name()))
                    .build();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
        Observable<Response> get = Rx.newClient(RxObservableInvoker.class, mes)
                .target(uri).request(MediaType.APPLICATION_JSON).rx().get();

        return get.map((response) -> {
            try {
                if (response.getStatus() == HttpStatus.SC_OK) {
                    String responseBody = response.readEntity(String.class);
                    JsonNavigator body = JsonNavigator.create(responseBody);
                    if (body.hasNext()) {
                        JsonNavigator obsParam = body.next();
                        String timestamp = obsParam.readToString("$.timestamp");
                        return timestampToXSDdateTime(Long.valueOf(timestamp))
                                .getLexicalForm();
                    }
                }

                return null;
            } catch (IOException ex) {
                throw Exceptions.propagate(ex);
            }
        });
    }

    public Observable<String> queryTimeOfLatestBySystemId(String systemId) {
        Observable<Response> get = Rx.newClient(RxObservableInvoker.class, mes)
                .target(UriBuilder.fromPath(config.tsdbEndpoint())
                        .path(QUERY_LAST_ENDPOINT)
                        .queryParam(PARAM_TIMESERIES, systemId).build())
                .request(MediaType.APPLICATION_JSON).rx().get();

        return get.map((response) -> {
            try {
                if (response.getStatus() == HttpStatus.SC_OK) {
                    String responseBody = response.readEntity(String.class);
                    JsonNavigator body = JsonNavigator.create(responseBody);
                    if (body.hasNext()) {
                        JsonNavigator obsParam = body.next();
                        String timestamp = obsParam.readToString("$.timestamp");
                        return timestampToXSDdateTime(Long.valueOf(timestamp))
                                .getLexicalForm();
                    }
                }

                return null;
            } catch (IOException ex) {
                throw Exceptions.propagate(ex);
            }
        });
    }

    public Observable<Model> queryLatestBySystemId(String systemId) {
        Observable<Response> get = Rx.newClient(RxObservableInvoker.class, mes)
                .target(UriBuilder.fromPath(config.tsdbEndpoint())
                        .path(QUERY_LAST_ENDPOINT)
                        .queryParam(PARAM_TIMESERIES, systemId)
                        .queryParam(PARAM_RESOLVE, true).build())
                .request(MediaType.APPLICATION_JSON).rx().get();

        return get.map((response) -> {
            try {
                Model model = ModelFactory.createDefaultModel();

                if (response.getStatus() == HttpStatus.SC_OK) {
                    JsonNavigator body = JsonNavigator
                            .create(response.readEntity(String.class));
                    while (body.hasNext()) {
                        JsonNavigator obsParam = body.next();
                        String timestamp = obsParam.readToString("$.timestamp");
                        String value = obsParam.readToString("$.value");
                        Resource valueType = decode(
                                obsParam.readToString("$.tags.value_type"));
                        Resource sensorUri = decode(
                                obsParam.readToString("$.tags.sensor_uri"));
                        Resource property = decode(
                                obsParam.readToString("$.tags.property"));
                        createObservation(model, valueType, sensorUri, property,
                                Long.valueOf(timestamp), value);
                    }
                }

                return model;
            } catch (IOException ex) {
                throw Exceptions.propagate(ex);
            }
        });
    }

    public Observable<Model> queryBySensorUri(String systemId, String uri,
            String start, String end) {
        UriBuilder uriBuilder = null;
        try {
            uriBuilder = UriBuilder.fromPath(config.tsdbEndpoint())
                    .path(QUERY_ENDPOINT).queryParam(PARAM_METRICS,
                            URLEncoder.encode("sum:" + systemId + "{sensor_uri="
                                    + encode(uri) + ",property=*,value_type=*}",
                                    StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
        if (Strings.isNullOrEmpty(start)) {
            throw new IllegalArgumentException();
        }
        uriBuilder.queryParam(PARAM_START, xsddateTimeToTSDBDateTime(start));

        if (!Strings.isNullOrEmpty(end)) {
            uriBuilder.queryParam(PARAM_END, xsddateTimeToTSDBDateTime(end));
        }

        Observable<Response> get = Rx.newClient(RxObservableInvoker.class, mes)
                .target(uriBuilder.build()).request(MediaType.APPLICATION_JSON)
                .rx().get();

        return get.map((response) -> {
            try {
                Model model = ModelFactory.createDefaultModel();

                if (response.getStatus() == HttpStatus.SC_OK) {
                    JsonNavigator body = JsonNavigator
                            .create(response.readEntity(String.class));
                    while (body.hasNext()) {
                        JsonNavigator obsParam = body.next();
                        Resource valueType = decode(
                                obsParam.readToString("$.tags.value_type"));
                        Resource sensorUri = decode(
                                obsParam.readToString("$.tags.sensor_uri"));
                        Resource property = decode(
                                obsParam.readToString("$.tags.property"));
                        Map<String, Object> obs = obsParam.readToMap("$.dps");
                        obs.keySet().stream().forEach((key) -> {
                            createObservation(model, valueType, sensorUri,
                                    property, Long.valueOf(key), obs.get(key));
                        });
                    }
                }

                return model;
            } catch (IOException ex) {
                throw Exceptions.propagate(ex);
            }
        });
    }

    public Observable<Model> queryBySystemId(String systemId, String start,
            String end) {
        UriBuilder uriBuilder = null;
        try {
            uriBuilder = UriBuilder.fromPath(config.tsdbEndpoint())
                    .path(QUERY_ENDPOINT).queryParam(PARAM_METRICS,
                            URLEncoder.encode(
                                    "sum:" + systemId
                                            + "{property=*,value_type=*}",
                                    StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
        if (Strings.isNullOrEmpty(start)) {
            throw new IllegalArgumentException();
        }
        uriBuilder.queryParam(PARAM_START, xsddateTimeToTSDBDateTime(start));

        if (!Strings.isNullOrEmpty(end)) {
            uriBuilder.queryParam(PARAM_END, xsddateTimeToTSDBDateTime(end));
        }

        Observable<Response> get = Rx.newClient(RxObservableInvoker.class, mes)
                .target(uriBuilder.build()).request(MediaType.APPLICATION_JSON)
                .rx().get();

        return get.map((response) -> {
            try {
                Model model = ModelFactory.createDefaultModel();

                if (response.getStatus() == HttpStatus.SC_OK) {
                    JsonNavigator body = JsonNavigator
                            .create(response.readEntity(String.class));
                    while (body.hasNext()) {
                        JsonNavigator obsParam = body.next();
                        Resource valueType = decode(
                                obsParam.readToString("$.tags.value_type"));
                        Resource sensorUri = decode(
                                obsParam.readToString("$.tags.sensor_uri"));
                        Resource property = decode(
                                obsParam.readToString("$.tags.property"));
                        Map<String, Object> obs = obsParam.readToMap("$.dps");
                        obs.keySet().stream().forEach((key) -> {
                            createObservation(model, valueType, sensorUri,
                                    property, Long.valueOf(key), obs.get(key));
                        });
                    }
                }

                return model;
            } catch (IOException ex) {
                throw Exceptions.propagate(ex);
            }
        });
    }

    @Override
    public Observable<ZonedDateTime> queryDateTimeOfLatestActuation(String systemId) {
        return null;
    }

    @Override
    public Observable<Model> queryActuationsByRange(String systemId, ZonedDateTime start, ZonedDateTime end) {
        return null;
    }

    private void createObservation(Model model, Resource valueType,
            Resource sensorUri, Resource property, long timestamp,
            Object value) {
        Resource observation = ResourceFactory.createResource();
        Resource obsResult = ResourceFactory.createResource();
        Resource obsValue = ResourceFactory.createResource();

        model.add(observation, RDF.type, SSN.Observaton)
                .add(observation, SSN.observedProperty, property)
                .add(observation, SSN.observedBy, sensorUri).add(observation,
                        SSN.observationResultTime,
                        timestampToXSDdateTime(timestamp));

        model.add(observation, SSN.observationResult, obsResult)
                .add(obsResult, RDF.type, SSN.SensorOutput)
                .add(obsResult, SSN.isProducedBy, sensorUri)
                .add(obsResult, SSN.hasValue, obsValue);

        model.add(obsValue, RDF.type, QUDT.QuantityValue).add(obsValue,
                QUDT.quantityValue, RDFUtils.toLiteral(value));
    }

    private Resource decode(String decodedUri)
            throws UnsupportedEncodingException {
        String strUri = URLDecoder.decode(decodedUri.replace(SLASH, PERCENT),
                StandardCharsets.UTF_8.name());

        return ResourceFactory.createResource(strUri);
    }

    private String encode(String uri) throws UnsupportedEncodingException {
        String urlEncoded = URLEncoder.encode(uri,
                StandardCharsets.UTF_8.name());

        return urlEncoded.replace(PERCENT, SLASH);
    }

    private Literal timestampToXSDdateTime(long timestamp) {
        String dateTime;
        if (timestamp < 9999999999L) {
            dateTime = Instant.ofEpochSecond(timestamp).toString();
        } else {
            dateTime = Instant.ofEpochMilli(timestamp).toString();
        }
        return ResourceFactory.createTypedLiteral(dateTime,
                XSDDatatype.XSDdateTime);
    }

    private String xsddateTimeToTSDBDateTime(String dateTime) {
        return ZonedDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME)
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm:ss")
                        .withZone(TimeZone.getTimeZone("UTC").toZoneId()));
    }

    @Override
    public Observable<String> queryTimeOfLatestBySystemId(String systemId,
            List<String> sensorsId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Observable<Model> queryLatestBySystemId(String systemId,
            List<String> sensorsId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Observable<Model> queryBySystemId(String systemId,
            List<String> sensorsId, String start, String end) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Observable<Response> remove(JsonArray array) {
        // TODO Auto-generated method stub
        return null;
    }

}
