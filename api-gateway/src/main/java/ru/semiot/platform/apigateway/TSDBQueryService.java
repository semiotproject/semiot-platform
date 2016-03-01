package ru.semiot.platform.apigateway;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.ejb.Singleton;
import javax.json.JsonValue;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.aeonbits.owner.ConfigFactory;
import org.apache.http.HttpStatus;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.glassfish.jersey.client.rx.Rx;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import ru.semiot.commons.namespaces.QUDT;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.platform.apigateway.utils.JsonNavigator;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import rx.Observable;
import rx.exceptions.Exceptions;

@Singleton
public class TSDBQueryService {

    private static final ServerConfig config = ConfigFactory.create(ServerConfig.class);
    private static final String SLASH = "/";
    private static final String PERCENT = "%";
    private static final String QUERY_ENDPOINT = "/api/query";
    private static final String PARAM_START = "start";
    private static final String PARAM_METRICS = "m";

    @javax.annotation.Resource
    ManagedExecutorService mes;

    public TSDBQueryService() {
    }

    public Observable<Model> query(String systemId) throws UnsupportedEncodingException {
        Observable<Response> get
                = Rx.newClient(RxObservableInvoker.class, mes)
                .target(UriBuilder
                        .fromPath(config.tsdbEndpoint())
                        .path(QUERY_ENDPOINT)
                        .queryParam(PARAM_START, "1h-ago")
                        .queryParam(PARAM_METRICS,
                                URLEncoder.encode("sum:" + systemId + "{property=*,value_type=*}",
                                        StandardCharsets.UTF_8.name()))
                        .build())
                .request(MediaType.APPLICATION_JSON)
                .rx()
                .get();

        return get.map((response) -> {
            try {
                Model model = ModelFactory.createDefaultModel();

                if (response.getStatus() == HttpStatus.SC_OK) {
                    JsonNavigator body = JsonNavigator.create(
                            response.readEntity(String.class));
                    while(body.hasNext()) {
                        JsonNavigator obsParam = body.next();
                        Resource valueType = decode(obsParam.readToString("$.tags.value_type"));
                        Resource sensorUri = decode(obsParam.readToString("$.tags.sensor_uri"));
                        Resource property = decode(obsParam.readToString("$.tags.property"));
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
    
    private void createObservation(Model model, Resource valueType, 
            Resource sensorUri, Resource property, long timestamp, Object value) {
        Resource observation = ResourceFactory.createResource();
        Resource obsResult = ResourceFactory.createResource();
        Resource obsValue = ResourceFactory.createResource();
        
        model.add(observation, RDF.type, SSN.Observaton)
                .add(observation, SSN.observedProperty, property)
                .add(observation, SSN.observedBy, sensorUri)
                .add(observation, SSN.observationResultTime, 
                        timestampToXSDdateTime(timestamp));
        
        model.add(observation, SSN.observationResult, obsResult)
                .add(obsResult, RDF.type, SSN.SensorOutput)
                .add(obsResult, SSN.isProducedBy, sensorUri)
                .add(obsResult, SSN.hasValue, obsValue);
        
        model.add(obsValue, RDF.type, QUDT.QuantityValue)
                .add(obsValue, QUDT.quantityValue, RDFUtils.toLiteral(value));
    }
    
    private Resource decode(String decodedUri) throws UnsupportedEncodingException {
        String strUri = URLDecoder.decode(decodedUri.replace(SLASH, PERCENT), 
                StandardCharsets.UTF_8.name());
        
        return ResourceFactory.createResource(strUri);
    }
    
    private Literal timestampToXSDdateTime(long timestamp) {
        String dateTime = Instant.ofEpochSecond(timestamp).toString();
        return ResourceFactory.createTypedLiteral(
                dateTime, XSDDatatype.XSDdateTime);
    }

}
