package ru.semiot.services.data_archiving_service;

import java.io.StringReader;
import java.util.LinkedList;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observer;

import ru.semiot.commons.namespaces.NamespaceUtils;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.commons.namespaces.SSNCOM;

public class SubscribeListener implements Observer<String> {

    private static final Logger logger = LoggerFactory
            .getLogger(SubscribeListener.class);
    private static final ServiceConfig config = ConfigFactory
            .create(ServiceConfig.class);
    private static final long TIMEOUT = 5000;
    private static final String VAR_TOPIC = "topic";
    private final HttpAuthenticator httpAuthenticator;
    private final WAMPClient wampClient = WAMPClient.getInstance();
    private final LinkedList<String> listTopics = new LinkedList<>();

    private static final Query GET_TOPICS_QUERY = QueryFactory.create(
            NamespaceUtils.newSPARQLQuery(
                    "SELECT ?topic { "
                    + "GRAPH <urn:semiot:graphs:private> {"
                    + " ?x ssncom:hasCommunicationEndpoint ?e ."
                    + " ?e ssncom:protocol \"WAMP\"; ssncom:topic ?topic ."
                    + "}"
                    + "}", SSN.class, SSNCOM.class));
    private static final String GET_TOPIC_BY_URI_QUERY
            = NamespaceUtils.newSPARQLQuery(
                    "SELECT ?topic { "
                    + "GRAPH <urn:semiot:graphs:private> {"
                    + " <${SYSTEM_URI}> ssncom:hasCommunicationEndpoint ?e ."
                    + " ?e ssncom:protocol \"WAMP\"; ssncom:topic ?topic ."
                    + "}"
                    + "}", SSN.class, SSNCOM.class);

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
                    new StringReader(message), null, RDFLanguages.TURTLE.getName());
            if (description != null && !description.isEmpty()) {
                subscribeTopics(description);
            } else {
                logger.warn("Received an empty message or in a wrong format!");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void subscribeTopics(Model description) {
        QueryExecution qe;
        ResultSet topics = null;
        if (description == null) {
            qe = getQEFromStoredTopics();
            boolean isConnected = false;
            while (!isConnected) {
                try {
                    topics = qe.execSelect();
                    logger.info("Connected to the triplestore");
                    isConnected = true;
                } catch (Exception ex) {
                    logger.warn(ex.getMessage());
                    logger.warn("Can`t connect to the triplestore! Retry in {}ms", TIMEOUT);
                    try {
                        Thread.sleep(TIMEOUT);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        } else {
            qe = getQEFromModelTopics(description);
            if (qe != null) {
                topics = qe.execSelect();
            } else {
                logger.error("Can'f find a ssn:System in device description!");
            }
        }
        while (topics != null && topics.hasNext()) {
            String topicName = topics.next().get(VAR_TOPIC)
                    .asLiteral().getString();
            if (StringUtils.isNotBlank(topicName)) {
                if (!listTopics.contains(topicName)) {
                    listTopics.add(topicName);
                    wampClient.addSubscription(
                            topicName,
                            wampClient.subscribe(topicName).subscribe(
                                    new WriterMetricsListener(topicName)));
                } else {
                    logger.debug("Topic {} is already known", topicName);
                }
            } else {
                logger.warn("Name topic is a blank string!");
            }
        }
    }

    private QueryExecution getQEFromStoredTopics() {
        return QueryExecutionFactory.sparqlService(config.storeUrl(),
                GET_TOPICS_QUERY, httpAuthenticator);
    }

    private QueryExecution getQEFromModelTopics(Model description) {
        ResIterator iter = description.listResourcesWithProperty(
                RDF.type, SSN.System);

        if (iter.hasNext()) {
            Resource system = iter.nextResource();
            return QueryExecutionFactory.sparqlService(
                    config.storeUrl(),
                    GET_TOPIC_BY_URI_QUERY.replace("${SYSTEM_URI}", system.getURI()),
                    httpAuthenticator);
        } else {
            return null;
        }
    }
}
