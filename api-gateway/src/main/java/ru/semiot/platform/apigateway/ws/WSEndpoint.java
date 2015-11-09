package ru.semiot.platform.apigateway.ws;

import com.hp.hpl.jena.query.QuerySolution;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.apigateway.MessageBusService;
import ru.semiot.platform.apigateway.SPARQLQueryService;
import rx.Subscription;
import rx.schedulers.Schedulers;

@ServerEndpoint("/ws/observations")
@Stateful
public class WSEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(WSEndpoint.class);
    private static final String SELECT_TOPIC_BY_SYSTEM_URI
            = "SELECT DISTINCT (str(?endpoint) as ?topic)\n"
            + "WHERE {\n"
            + "  <${SYSTEM_URI}> ssn:hasSubSystem ?sensor .\n"
            + "  ?sensor ssncom:hasCommunicationEndpoint ?endpoint .\n"
            + "  ?endpoint ssncom:protocol \"WAMP\" .\n"
            + "}";
    private static final String SYSTEM_URI_KEY = "${SYSTEM_URI}";

    @Inject
    SPARQLQueryService query;

    @Inject
    MessageBusService message;

    @Resource
    ManagedExecutorService executor;

    private final List<Subscription> subscriptions = 
            Collections.synchronizedList(new ArrayList<>());

    @OnOpen
    public void open(Session session) throws IOException {
        logger.info("[{}][{}] was opened!", this, session.getId());

        final URI systemUri = extractSystemUri(session.getQueryString());
        if (systemUri == null) {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, null));

            return;
        }

        query.select(
                SELECT_TOPIC_BY_SYSTEM_URI
                .replace(SYSTEM_URI_KEY, systemUri.toASCIIString())
        ).observeOn(Schedulers.from(executor)).subscribe((r) -> {
            while (r.hasNext()) {
                final QuerySolution qs = r.next();
                final String topicUri = qs.getLiteral("topic").getString();
                final String wampTopic = extractWampTopic(topicUri);

                subscribeToTopic(session, wampTopic);
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
                    System.out.println(m);

                    session.getAsyncRemote().sendText(m);
                }, (e) -> {
                    logger.warn(e.getMessage(), e);
                });

        subscriptions.add(subscription);
    }

    private URI extractSystemUri(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return null;
        }

        if (queryString.matches("system_uri=.+")) {
            try {
                return new URI(queryString.substring(11));
            } catch (URISyntaxException ex) {
                return null;
            }
        } else {
            return null;
        }
    }

    private String extractWampTopic(String uri) {
        return uri.substring(uri.indexOf("topic=") + 6).trim();
    }

}
