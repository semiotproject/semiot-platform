package ru.semiot.services.data_archiving_service;

import java.io.StringReader;
import java.util.LinkedList;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observer;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

public class SubscribeListener implements Observer<String> {

    private static final Logger logger = LoggerFactory
            .getLogger(SubscribeListener.class);
    public static final String LANG = "TURTLE";
    private static final ServiceConfig config = ConfigFactory
            .create(ServiceConfig.class);
    private final HttpAuthenticator httpAuthenticator;
    private final WAMPClient wampClient = WAMPClient.getInstance();
    private static final String PREFIX_TOPIC = "topic=";
    private LinkedList<String> listTopics = new LinkedList<String>();

    private static final Query GET_TOPICS_QUERY = QueryFactory.create(
            "PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#>\n"
            + "PREFIX ssncom: <http://purl.org/NET/ssnext/communication#>\n"
            + "SELECT ?q { "
            + "GRAPH <urn:semiot:graphs:private> {"
            + " ?x ssncom:hasCommunicationEndpoint ?e ."
            + " ?e ssncom:protocol \"WAMP\"; ssncom:topic ?q ."
            + "}"
            + "}");
    private static final String GET_TOPIC_BY_URI_QUERY
            = "PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#>\n"
            + "PREFIX ssncom: <http://purl.org/NET/ssnext/communication#>\n"
            + "SELECT ?q { "
            + "GRAPH <urn:semiot:graphs:private> {"
            + " <${SYSTEM_URI}> ssncom:hasCommunicationEndpoint ?e ."
            + " ?e ssncom:protocol \"WAMP\"; ssncom:topic ?q ."
            + "}"
            + "}";
    private static final Resource SSN_SYSTEM = ResourceFactory.createResource(
            "http://purl.oclc.org/NET/ssnx/ssn#System");

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

    /* private String parseTopicName(String uri) {
     int index = uri.indexOf(PREFIX_TOPIC);
     if (index != -1) {
     int lastIndex = uri.lastIndexOf(">", index);
     return index + PREFIX_TOPIC.length() + 2 > uri.length() ? null
     : uri.substring(index + PREFIX_TOPIC.length(),
     lastIndex == -1 ? uri.length() : lastIndex);
     } else {
     return null;
     }
     } */
    private void subscribeTopics(Model description) {
        QueryExecution qe;
        ResultSet topics = null;
        if (description == null) {
            qe = getQEFromStoredTopics();
            boolean isConnected = false;
            while (!isConnected) {
                try {
                    topics = qe.execSelect();
                    logger.info("Connect to fuseki successfuly.");
                    isConnected = true;
                } catch (Exception ex) {
                    logger.warn(ex.getMessage());
                    logger.warn("Can`t connect to fuseki. A new connection attempt after 5 seconds.");
                    try {
                        Thread.sleep(5000);
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
            String topicName = topics.next().get("q")
                    .asLiteral().getString();
            if (StringUtils.isNotBlank(topicName)
                    && !listTopics.contains(topicName)) {
                listTopics.add(topicName);
                wampClient.addSubscription(
                        topicName,
                        wampClient.subscribe(topicName).subscribe(
                                new WriterMetricsListener(topicName)));
            } else {
                logger.warn("Name topic is blank");
            }
        }
    }

    private QueryExecution getQEFromStoredTopics() {
        return QueryExecutionFactory.sparqlService(config.storeUrl(),
                GET_TOPICS_QUERY, httpAuthenticator);
    }

    private QueryExecution getQEFromModelTopics(Model description) {
        ResIterator iter = description.listResourcesWithProperty(
                RDF.type, SSN_SYSTEM);
        
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
