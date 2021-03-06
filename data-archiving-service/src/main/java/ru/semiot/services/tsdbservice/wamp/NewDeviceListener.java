package ru.semiot.services.tsdbservice.wamp;

import static ru.semiot.services.tsdbservice.ServiceConfig.CONFIG;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.DCTerms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.NamespaceUtils;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.commons.namespaces.SSNCOM;
import rx.Observer;
import ws.wamp.jawampa.SubscriptionFlags;

import java.io.StringReader;
import java.util.LinkedList;

public class NewDeviceListener implements Observer<String> {

  private static final Logger logger = LoggerFactory.getLogger(NewDeviceListener.class);
  private static final long TIMEOUT = 5000;
  private static final String VAR_OBSERVATIONS_TOPIC = "obs_topic";
  private static final String VAR_COMMANDRESULTS_TOPIC = "commres_topic";
  private static final String VAR_SYSTEM_ID = "system_id";
  private static final String TOPIC_OBSERVATIONS = "${SYSTEM_ID}.observations";
  private static final String TOPIC_COMMANDRESULT = "${SYSTEM_ID}.commandresults";
  private final HttpAuthenticator httpAuthenticator;
  private final WAMPClient wampClient = WAMPClient.getInstance();
  private final LinkedList<String> listTopics = new LinkedList<>();

  private static final Query GET_TOPICS_QUERY = QueryFactory.create(NamespaceUtils.newSPARQLQuery(
      "SELECT ?system_id ?obs_topic ?commres_topic { "
          + "?x dcterms:identifier ?system_id. "
          + " ?x ssncom:hasCommunicationEndpoint ?e ."
          + " ?e ssncom:protocol \"WAMP\"; "
          + "   ssncom:provide \"observations\"; ssncom:topic ?obs_topic ."
          + " ?x ssncom:hasCommunicationEndpoint ?ee ."
          + " ?ee ssncom:protocol \"WAMP\";"
          + "   ssncom:provide \"commandresults\"; ssncom:topic ?commres_topic ."
          + "}", SSN.class, SSNCOM.class, DCTerms.class));

  private static final String GET_SYSTEM_ID = NamespaceUtils.newSPARQLQuery(
      "SELECT ?system_id { ?system a ssn:System. ?system dcterms:identifier ?system_id. }",
      SSN.class, DCTerms.class);

  public NewDeviceListener() {
    httpAuthenticator = new SimpleAuthenticator(
        CONFIG.storeUsername(), CONFIG.storePassword().toCharArray());
  }

  @Override
  public void onCompleted() {}

  @Override
  public void onError(Throwable e) {
    logger.warn(e.getMessage(), e);
  }

  @Override
  public void onNext(String message) {
    try {
      long startTimestamp = System.currentTimeMillis();
      Model description = ModelFactory.createDefaultModel().read(
          new StringReader(message), null, RDFLanguages.strLangJSONLD);
      if (description != null && !description.isEmpty()) {
        String systemId = getSystemId(description);
        if (StringUtils.isNoneBlank(systemId)) {
          subscribeToDeviceTopic(systemId);
          logger.debug("Subscription topic is made in {} seconds",
              System.currentTimeMillis() - startTimestamp);
        } else {
          logger.error("Can'f find a ssn:System in device description!");
        }
      } else {
        logger.warn("Received an empty message or in a wrong format!");
      }
    } catch (Throwable ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  public void loadDeviceTopicsAndSubscribe() {
    QueryExecution qe = getQEFromStoredTopics();
    boolean isConnected = false;
    while (!isConnected) {
      try {
        ResultSet topics = qe.execSelect();
        logger.info("Connected to triplestore");
        isConnected = true;

        subscribeToDevicesTopics(topics);
      } catch (Throwable ex) {
        logger.warn(ex.getMessage());
        logger.warn("Can`t connect to the triplestore! Retry in {} ms", TIMEOUT);
        try {
          Thread.sleep(TIMEOUT);
        } catch (InterruptedException e) {
          logger.error(e.getMessage());
        }
      }
    }
  }

  private void subscribeToDevicesTopics(ResultSet topics) {
    while (topics != null && topics.hasNext()) {
      QuerySolution qs = topics.next();
      String topicObsName = qs.get(VAR_OBSERVATIONS_TOPIC).asLiteral().getString();
      String topicCommResName = qs.get(VAR_COMMANDRESULTS_TOPIC).asLiteral().getString();
      String systemId = qs.get(VAR_SYSTEM_ID).asLiteral().getString();
      subscriveToDeviceTopics(topicObsName, topicCommResName, systemId);
    }
  }

  private void subscribeToDeviceTopic(String systemId) {
    subscriveToDeviceTopics(TOPIC_OBSERVATIONS.replace("${SYSTEM_ID}", systemId),
        TOPIC_COMMANDRESULT.replace("${SYSTEM_ID}", systemId), systemId);
  }

  private void subscriveToDeviceTopics(String topicObsName, String topicCommResName,
      String systemId) {
    if (StringUtils.isNotBlank(topicObsName) && StringUtils.isNotBlank(topicCommResName)
        && StringUtils.isNotBlank(systemId)) {
      if (!listTopics.contains(topicObsName) && !listTopics.contains(topicCommResName)) {
        listTopics.add(topicObsName);
        listTopics.add(topicCommResName);
        // Subscribe to observations
        wampClient.addSubscription(topicObsName,
            wampClient.subscribe(topicObsName, SubscriptionFlags.Prefix)
                .subscribe(new ObservationListener(systemId)));
        // Subscribe to command results
        wampClient.addSubscription(topicCommResName,
            wampClient.subscribe(topicCommResName, SubscriptionFlags.Prefix)
                .subscribe(new CommandResultListener()));
      } else {
        logger.debug("Topics {} and {} are already known", topicObsName, topicCommResName);
      }
    } else {
      logger.warn("Name topic is a blank string!");
    }
  }

  private QueryExecution getQEFromStoredTopics() {
    return QueryExecutionFactory.sparqlService(CONFIG.storeUrl(),
        GET_TOPICS_QUERY, httpAuthenticator);
  }

  private String getSystemId(Model description) {
    ResultSet rs = QueryExecutionFactory.create(GET_SYSTEM_ID, description).execSelect();
    if (rs.hasNext()) {
      return rs.next().getLiteral(VAR_SYSTEM_ID).getString();
    }
    return null;
  }

}
