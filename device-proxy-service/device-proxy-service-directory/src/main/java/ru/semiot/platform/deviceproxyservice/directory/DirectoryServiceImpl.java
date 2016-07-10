package ru.semiot.platform.deviceproxyservice.directory;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.NamespaceUtils;
import ru.semiot.commons.namespaces.SEMIOT;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.commons.namespaces.SSNCOM;
import ru.semiot.platform.deviceproxyservice.api.drivers.Configuration;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DriverInformation;
import ru.semiot.platform.deviceproxyservice.api.manager.DirectoryService;

import java.net.URI;
import java.util.Dictionary;

public class DirectoryServiceImpl implements DirectoryService, ManagedService {

  private static final Logger logger = LoggerFactory.getLogger(DirectoryServiceImpl.class);
  private static final Literal WAMP = ResourceFactory.createPlainLiteral("WAMP");
  private static final String GRAPH_PRIVATE = "urn:semiot:graphs:private";
  private static final String TEMPLATE_DRIVER_URN = "urn:semiot:drivers:${PID}";
  private static final String TOPIC_SENSOR_OBSERVATIONS = "${SYSTEM_ID}.observations.${SENSOR_ID}";
  private static final String TOPIC_OBSERVATIONS = "${SYSTEM_ID}.observations";
  private static final String TOPIC_COMMANDRESULT = "${SYSTEM_ID}.commandresults";
  private static final String GET_SYSTEM_URI =
      NamespaceUtils.newSPARQLQuery("SELECT ?uri {?uri a ssn:System} LIMIT 1", SSN.class);
  private static final String GET_SENSOR = NamespaceUtils.newSPARQLQuery(
      "SELECT ?sensor_uri ?sensor_id {<${URI_SYSTEM}> ssn:hasSubSystem ?sensor_uri. "
          + "?sensor_uri a ssn:SensingDevice. ?sensor_uri dcterms:identifier ?sensor_id}",
      SSN.class, DCTerms.class);
  private static final String GET_DRIVER_PID_BY_SYSTEM_ID = NamespaceUtils.newSPARQLQuery(
      "SELECT ?pid {" + "?system dcterms:identifier \"${SYSTEM_ID}\" ."
          + "?system semiot:hasDriver ?pid"
          + "} LIMIT 1",
      DCTerms.class, SEMIOT.class);

  private final Configuration configuration = new Configuration();
  private RDFStore store;

  public DirectoryServiceImpl() {}

  public void start() {
    store = new RDFStore(configuration);
  }

  public void stop() {
    store = null;
  }

  @Override
  public void updated(Dictionary dictionary) throws ConfigurationException {
    synchronized (this) {
      if (dictionary != null) {
        if (!configuration.isConfigured()) {
          //default values
          configuration.put(Keys.TRIPLESTORE_USERNAME, "admin");
          configuration.put(Keys.TRIPLESTORE_PASSWORD, "pw");
          configuration.put(Keys.TRIPLESTORE_QUERY_URL,
              "http://triplestore:3030/blazegraph/sparql");
          configuration.put(Keys.TRIPLESTORE_UPDATE_URL,
              "http://triplestore:3030/blazegraph/sparql");
          configuration.put(Keys.TRIPLESTORE_STORE_URL,
              "http://triplestore:3030/blazegraph/sparql");

          configuration.putAll(dictionary);

          configuration.setConfigured();

          logger.debug("Bundle was configured");
        } else {
          logger.warn("Bundle is already configured. Ignoring it");
        }
      } else {
        logger.debug("Configuration is empty. Skipping it");
      }
    }
  }

  public void loadDevicePrototype(URI uri) {
    try {
      Model model = RDFDataMgr.loadModel(uri.toASCIIString(), RDFLanguages.TURTLE);

      store.save(model);
    } catch (Throwable ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  public boolean addNewDevice(DriverInformation info, Device device, Model description) {
    try {
      if (!description.isEmpty()) {
        ResultSet qr = QueryExecutionFactory.create(GET_SYSTEM_URI, description).execSelect();

        if (qr.hasNext()) {
          Resource system = qr.next().getResource(Vars.URI);

          // Given uri is not a blank node
          if (system.isURIResource()) {
            store.save(description);

            // Add ssncom:hasCommunicationEndpoint to a private graph
            Model privateDeviceInfo = ModelFactory.createDefaultModel();

            Resource wampResourceObs =
                ResourceFactory.createResource(system.getURI() + "/observations/wamp");
            addWampForResource(privateDeviceInfo, system, wampResourceObs,
                TOPIC_OBSERVATIONS.replace("${SYSTEM_ID}", device.getId()));
            privateDeviceInfo.add(wampResourceObs, SSNCOM.provide,
                ResourceFactory.createPlainLiteral("observations"));

            Resource wampResourceCommRes =
                ResourceFactory.createResource(system.getURI() + "/commandresults/wamp");
            addWampForResource(privateDeviceInfo, system, wampResourceCommRes,
                TOPIC_COMMANDRESULT.replace("${SYSTEM_ID}", device.getId()));
            privateDeviceInfo.add(wampResourceCommRes, SSNCOM.provide,
                ResourceFactory.createPlainLiteral("commandresults"));

            privateDeviceInfo.add(system, SEMIOT.hasDriver, ResourceFactory
                .createResource(TEMPLATE_DRIVER_URN.replace("${PID}", info.getId())));

            ResultSet sensors = QueryExecutionFactory
                .create(GET_SENSOR.replace("${URI_SYSTEM}", system.getURI()), description)
                .execSelect();
            if (sensors.hasNext()) {
              QuerySolution qs = sensors.next();
              Resource sensor = qs.getResource(Vars.URI_SENSOR);
              String sensorId = qs.getLiteral(Vars.ID_SENSOR).getString();
              Resource sensorWampResource =
                  ResourceFactory.createResource(sensor.getURI() + "/wamp");
              addWampForResource(privateDeviceInfo, sensor, sensorWampResource,
                  TOPIC_SENSOR_OBSERVATIONS.replace("${SYSTEM_ID}", device.getId())
                      .replace("${SENSOR_ID}", sensorId));
            }

            store.save(GRAPH_PRIVATE, privateDeviceInfo);

            return true;
          } else {
            logger.error("Device [{}] doesn't have URI!", device.getId());
          }
        } else {
          logger.error("Can't find a system!");
        }
      } else {
        logger.warn("Received an empty message or in a wrong format!");
      }
    } catch (RiotException ex) {
      logger.warn(ex.getMessage(), ex);
    } catch (Throwable ex) {
      logger.error(ex.getMessage(), ex);
    }

    return false;
  }

  private void addWampForResource(Model model, Resource res, Resource wampResource, String topic) {
    model.add(res, SSNCOM.hasCommunicationEndpoint, wampResource)
        .add(wampResource, RDF.type, SSNCOM.CommunicationEndpoint)
        .add(wampResource, SSNCOM.topic,
            ResourceFactory.createPlainLiteral(topic))
        .add(wampResource, SSNCOM.protocol, WAMP);
  }

  public String findDriverPidByDeviceId(String deviceId) {
    String query = GET_DRIVER_PID_BY_SYSTEM_ID.replace("${SYSTEM_ID}", deviceId);
    ResultSet resultSet = store.select(query);
    if (resultSet.hasNext()) {
      QuerySolution qs = resultSet.next();
      Resource pid = qs.getResource("pid");
      String driverUri = pid.getURI();
      return driverUri.replace("urn:semiot:drivers:", "");
    } else {
      return null;
    }
  }

  private static abstract class Vars {
    public static final String URI = "uri";
    public static final String URI_SENSOR = "sensor_uri";
    public static final String ID_SENSOR = "sensor_id";
  }
}
