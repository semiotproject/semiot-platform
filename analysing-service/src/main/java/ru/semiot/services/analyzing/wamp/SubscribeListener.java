package ru.semiot.services.analyzing.wamp;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.StringReader;
import java.util.LinkedList;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static ru.semiot.services.analyzing.ServiceConfig.config;
import rx.Observer;

public class SubscribeListener implements Observer<String> {

    private static final Logger logger = LoggerFactory
            .getLogger(SubscribeListener.class);
    public static final String LANG = "TURTLE";
    private final HttpAuthenticator httpAuthenticator;
    private final WAMPClient wampClient = WAMPClient.getInstance();
    private static final String PREFIX_TOPIC = "topic=";
    private LinkedList<String> listTopics = new LinkedList<String>();

    private static final Query TOPICS_QUERY = QueryFactory
            .create(new StringBuilder()
                    .append("prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> ")
                    .append("prefix ssncom: <http://purl.org/NET/ssnext/communication#> ")
                    .append("SELECT ?q where{ ?x ssncom:hasCommunicationEndpoint ?e. ")
                    .append("?e ssncom:protocol \"WAMP\"; ssncom:topic ?q}").toString());

    public SubscribeListener() {
        httpAuthenticator = new SimpleAuthenticator(config.storeUsername(),
                config.storePassword().toCharArray());
        subscribeTopics(null);
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        logger.warn(e.getMessage(), e);
    }

    @Override
    public void onNext(String message) {
        try {
            Model description = ModelFactory.createDefaultModel().read(
                    new StringReader(message), null, LANG);
            if (description != null && !description.isEmpty()) {
                subscribeTopics(description);
            } else {
                logger.warn("Received an empty message or in a wrong format!");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private String parseTopicName(String uri) {
        int index = uri.indexOf(PREFIX_TOPIC);
        if (index != -1) {
            int lastIndex = uri.lastIndexOf(">", index);
            return index + PREFIX_TOPIC.length() + 2 > uri.length() ? null
                    : uri.substring(index + PREFIX_TOPIC.length(),
                            lastIndex == -1 ? uri.length() : lastIndex);
        } else {
            return null;
        }
    }

    private void subscribeTopics(Model description) {

        QueryExecution qe;
        if (description == null) {
            qe = getQEFromStoredTopics();
        } else {
            qe = getQEFromModelTopics(description);
        }

        ResultSet topics = qe.execSelect();
        while (topics.hasNext()) {
            String topicName = topics.next().get("q").asLiteral().getString();
            if (!topicName.isEmpty()
                    && !listTopics.contains(topicName)) {
                listTopics.add(topicName);
                wampClient.subscribe(topicName).subscribe(
                        new TopicListener(topicName));
            } else {
                logger.warn("Name topic is blank");
            }
        }

    }

    private QueryExecution getQEFromStoredTopics() {
        return QueryExecutionFactory.sparqlService(config.storeUrl(),
                TOPICS_QUERY, httpAuthenticator);
    }

    private QueryExecution getQEFromModelTopics(Model description) {
        return QueryExecutionFactory.create(TOPICS_QUERY, description);
    }
}
