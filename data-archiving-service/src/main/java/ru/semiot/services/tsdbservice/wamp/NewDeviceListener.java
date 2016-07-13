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
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
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
  private final HttpAuthenticator httpAuthenticator;
  private final WAMPClient wampClient = WAMPClient.getInstance();
  private final LinkedList<String> listTopics = new LinkedList<>();

  private static final Query GET_TOPICS_QUERY = QueryFactory.create(NamespaceUtils.newSPARQLQuery(
      "SELECT ?system_id ?obs_topic ?commres_topic { "
          + "?x dcterms:identifier ?system_id. "
//          + "GRAPH <urn:semiot:graphs:private> {"
          + " ?x ssncom:hasCommunicationEndpoint ?e ."
          + " ?e ssncom:protocol \"WAMP\"; "
          + "   ssncom:provide \"observations\"; ssncom:topic ?obs_topic ."
          + " ?x ssncom:hasCommunicationEndpoint ?ee ."
          + " ?ee ssncom:protocol \"WAMP\";"
          + "   ssncom:provide \"commandresults\"; ssncom:topic ?commres_topic ."
//          + "}"
          + "}", SSN.class, SSNCOM.class, DCTerms.class));
  private static final String GET_TOPIC_BY_URI_QUERY = NamespaceUtils.newSPARQLQuery(
      "SELECT ?system_id ?obs_topic ?commres_topic { "
          + "<${SYSTEM_URI}> dcterms:identifier ?system_id. "
//          + "GRAPH <urn:semiot:graphs:private> {"
          + " <${SYSTEM_URI}> ssncom:hasCommunicationEndpoint ?e ."
          + " ?e ssncom:protocol \"WAMP\"; "
          + "   ssncom:provide \"observations\"; ssncom:topic ?obs_topic ."
          + " <${SYSTEM_URI}> ssncom:hasCommunicationEndpoint ?ee ."
          + " ?ee ssncom:protocol \"WAMP\"; "
          + "   ssncom:provide \"commandresults\"; ssncom:topic ?commres_topic ."
//          + "}"
          + "}", SSN.class, SSNCOM.class, DCTerms.class);

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
        QueryExecution qe = getQEFromModelTopics(description);
        ResultSet topics = null;
        if (qe != null) {
          topics = qe.execSelect();
        } else {
          logger.error("Can'f find a ssn:System in device description!");
        }

        subscribeToDeviceTopics(topics);
        logger.debug("Subscription topics is made in {} seconds",
            System.currentTimeMillis() - startTimestamp);
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

        subscribeToDeviceTopics(topics);
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

  private void subscribeToDeviceTopics(ResultSet topics) {
    while (topics != null && topics.hasNext()) {
      QuerySolution qs = topics.next();
      String topicObsName = qs.get(VAR_OBSERVATIONS_TOPIC).asLiteral().getString();
      String topicCommResName = qs.get(VAR_COMMANDRESULTS_TOPIC).asLiteral().getString();
      String systemId = qs.get(VAR_SYSTEM_ID).asLiteral().getString();
      if (StringUtils.isNotBlank(topicObsName) && StringUtils.isNotBlank(topicCommResName)
          && StringUtils.isNotBlank(systemId)) {
        if (!listTopics.contains(topicObsName) && !listTopics.contains(topicCommResName)) {
          listTopics.add(topicObsName);
          listTopics.add(topicCommResName);
          //Subscribe to observations
          wampClient.addSubscription(topicObsName,
              wampClient.subscribe(topicObsName, SubscriptionFlags.Prefix)
                  .subscribe(new ObservationListener(systemId)));
          //Subscribe to command results
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
  }

  private QueryExecution getQEFromStoredTopics() {
    return QueryExecutionFactory.sparqlService(CONFIG.storeUrl(),
        GET_TOPICS_QUERY, httpAuthenticator);
  }

  private QueryExecution getQEFromModelTopics(Model description) {
    ResIterator iter = description.listResourcesWithProperty(RDF.type, SSN.System);

    if (iter.hasNext()) {
      Resource system = iter.nextResource();
      return QueryExecutionFactory.sparqlService(
          CONFIG.storeUrl(),
          GET_TOPIC_BY_URI_QUERY.replace("${SYSTEM_URI}", system.getURI()),
          httpAuthenticator);
    } else {
      return null;
    }
  }
}
