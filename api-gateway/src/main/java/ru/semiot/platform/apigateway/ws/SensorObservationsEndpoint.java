package ru.semiot.platform.apigateway.ws;

import java.io.IOException;
import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.apigateway.MessageBusService;
import ru.semiot.platform.apigateway.SPARQLQueryService;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import rx.Subscription;
import rx.schedulers.Schedulers;

@ServerEndpoint("/ws/sensors/{id}/observations")
@Stateful
public class SensorObservationsEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(SensorObservationsEndpoint.class);

    private static final String SELECT_TOPIC_BY_SENSOR_ID
            = "SELECT ?sensor_uri ?topic {"
            + " ?sensor_uri dcterms:identifier \"${SENSOR_ID}\"^^xsd:string ;"
            + "   	ssn:observes ?property ."
            + "	?system_uri ssn:hasSubSystem ?sensor_uri ;"
            + "    	ssncom:hasCommunicationEndpoint ?endpoint ."
            + " ?endpoint ssncom:protocol \"WAMP\" ;"
            + "    	ssncom:topic ?topic ."
            + "}";

    private static final String PARAM_ID = "id";
    private static final String KEY_SENSOR_ID = "${SENSOR_ID}";
    private static final String VAR_TOPIC = "topic";
    private static final String VAR_SENSOR_URI = "sensor_uri";
    private static final String SSN_OBSERVEDBY = "http://purl.oclc.org/NET/ssnx/ssn#observedBy";

    @Inject
    SPARQLQueryService query;
    @Inject
    MessageBusService message;
    @Resource
    ManagedExecutorService executor;

    private Subscription subscription;

    @OnOpen
    public void open(Session session) throws IOException {
        logger.info("[{}][{}] was opened!", this, session.getId());

        final String id = session.getPathParameters().get(PARAM_ID);

        if (id == null || id.isEmpty()) {
            session.close(new CloseReason(
                    CloseReason.CloseCodes.VIOLATED_POLICY, null));

            return;
        }

        query.select(SELECT_TOPIC_BY_SENSOR_ID.replace(KEY_SENSOR_ID, id))
                .observeOn(Schedulers.from(executor)).subscribe((r) -> {
                    if (r.hasNext()) {
                        final QuerySolution qs = r.next();
                        final String topic = qs.getLiteral(VAR_TOPIC).getString();
                        final String sensorUri = qs.getResource(VAR_SENSOR_URI).getURI();

                        filterAndProxyObservations(topic, sensorUri, session);
                    } else {
                        try {
                            logger.info("[{}][{}] sensor by [{}] id wasn't found!", 
                                    this, session.getId(), id);

                            session.close(new CloseReason(
                                    CloseReason.CloseCodes.VIOLATED_POLICY, null));
                        } catch (IOException ex) {
                            logger.warn(ex.getMessage(), ex);
                        }
                    }
                }, (e) -> {
                    logger.warn(e.getMessage(), e);

                    try {
                        session.close();
                    } catch (IOException ex) {
                        logger.warn(ex.getMessage(), ex);
                    }
                });
    }

    @OnError
    public void error(Session session, Throwable thrw) {
        logger.warn(thrw.getMessage(), thrw);
    }

    @OnClose
    public void close(Session session) {
        if (subscription != null) {
            subscription.unsubscribe();
        }

        logger.info("[{}][{}] was closed!", this, session.getId());
    }

    private void filterAndProxyObservations(
            String topic, String uri, Session session) {
        this.subscription = message.subscribe(topic)
                .observeOn(Schedulers.from(executor))
                .subscribe((m) -> {
                    if (RDFUtils.match(m,
                            Node.ANY,
                            ResourceFactory.createProperty(SSN_OBSERVEDBY).asNode(),
                            ResourceFactory.createResource(uri).asNode())) {
                        logger.info(m);
                        session.getAsyncRemote().sendText(m);
                    }
                }, (e) -> {
                    logger.warn(e.getMessage(), e);
                });
    }

}
