package ru.semiot.platform.apigateway.ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import org.apache.jena.query.QuerySolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.apigateway.beans.impl.MessageBusService;
import ru.semiot.platform.apigateway.beans.impl.SPARQLQueryService;
import rx.Subscription;
import rx.schedulers.Schedulers;

@ServerEndpoint("/ws/systems/{id}/observations")
@Stateful
public class SystemObservationsEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(
            SystemObservationsEndpoint.class);
    private static final String SELECT_TOPIC_BY_SYSTEM_ID
            = "SELECT ?topic {"
            + "?system_uri dcterms:identifier \"${SYSTEM_ID}\"^^xsd:string ;"
            + "ssncom:hasCommunicationEndpoint/ssncom:topic ?topic ."
            + "}\n"
            + "LIMIT 1";
    private static final String PARAM_ID = "id";
    private static final String SYSTEM_ID_KEY = "${SYSTEM_ID}";
    private static final String VAR_TOPIC = "topic";

    @Inject
    SPARQLQueryService query;

    @Inject
    MessageBusService message;

    @Resource
    ManagedExecutorService executor;

    private final List<Subscription> subscriptions
            = Collections.synchronizedList(new ArrayList<>());

    @OnOpen
    public void open(Session session) throws IOException {
        logger.info("[{}][{}] was opened!", this, session.getId());

        final String id = session.getPathParameters().get(PARAM_ID);

        if (id == null || id.isEmpty()) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, null));

            return;
        }

        query.select(SELECT_TOPIC_BY_SYSTEM_ID.replace(SYSTEM_ID_KEY, id))
                .observeOn(Schedulers.from(executor)).subscribe((r) -> {
                    if (r.hasNext()) {
                        while (r.hasNext()) {
                            final QuerySolution qs = r.next();
                            final String wampTopic = qs.getLiteral(VAR_TOPIC).getString();

                            subscribeToTopic(session, wampTopic);
                        }
                    } else {
                        try {
                            logger.info("[{}][{}] system by [] id wasn't found!", this, session.getId(), id);
                            
                            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, null));
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
        if (subscriptions.size() > 0) {
            subscriptions.stream().forEach((s) -> {
                s.unsubscribe();
            });

            subscriptions.clear();
        }

        logger.info("[{}][{}] was closed!", this, session.getId());
    }

    private void subscribeToTopic(Session session, String topic) {
        Subscription subscription = message.subscribe(topic)
                .observeOn(Schedulers.from(executor))
                .subscribe((m) -> {
                    logger.debug(m);

                    session.getAsyncRemote().sendText(m);
                }, (e) -> {
                    logger.warn(e.getMessage(), e);
                });

        subscriptions.add(subscription);
    }

}
