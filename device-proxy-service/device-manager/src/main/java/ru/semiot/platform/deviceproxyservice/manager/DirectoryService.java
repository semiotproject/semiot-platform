package ru.semiot.platform.deviceproxyservice.manager;

import org.apache.jena.query.QueryExecution;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.GEO;
import ru.semiot.commons.namespaces.NamespaceUtils;
import ru.semiot.commons.namespaces.Proto;
import ru.semiot.commons.namespaces.SAREF;
import ru.semiot.commons.namespaces.SEMIOT;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.commons.namespaces.SSNCOM;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DriverInformation;

import java.io.StringReader;
import java.net.URI;

public class DirectoryService {

  private static final Logger logger = LoggerFactory.getLogger(DirectoryService.class);
  private static final Literal WAMP = ResourceFactory.createPlainLiteral("WAMP");
  private static final String GRAPH_PRIVATE = "urn:semiot:graphs:private";
  private static final String TEMPLATE_DRIVER_URN = "urn:semiot:drivers:${PID}";
  private static final String TOPIC_SENSOR_OBSERVATIONS = "${SYSTEM_ID}.observations.${SENSOR_ID}";
  private static final String TOPIC_OBSERVATIONS = "${SYSTEM_ID}.observations";
  private static final String TOPIC_ACTUATIONS = "${SYSTEM_ID}.commandresults";

  protected static final String QUERY_DELETE_ALL_DATA_DRIVER = NamespaceUtils.newSPARQLQuery(
      // @formatter:off
      "DELETE { "
      + "?system ?x1 ?y1. ?sensor ?x2 ?y2. "
      + "?prototype ?x3 ?y3. ?mc ?x4 ?y4. "
      + "?mp ?x5 ?y5. ?value ?x6 ?y6. ?loc ?x7 ?y7.  "
      + "GRAPH <urn:semiot:graphs:private>{"
        + "?system ?x8 ?y8. ?wamp ?x9 ?y9} }"
      + "  WHERE { "
        + "GRAPH <urn:semiot:graphs:private> {"
          + "?system semiot:hasDriver <urn:semiot:drivers:${PID}>} . "
        + "{ ?system  ssn:hasSubSystem  ?sensor . "
          + "?sensor proto:hasPrototype ?prototype . "
          + "?prototype ssn:hasMeasurementCapability ?mc . "
          + "?mc ssn:hasMeasurementProperty ?mp . "
          + "?mp ssn:hasValue ?value . "
          + "?system ?x1 ?y1. ?sensor ?x2 ?y2. ?prototype ?x3 ?y3. "
          + "?mc ?x4 ?y4. ?mp ?x5 ?y5. ?value ?x6 ?y6 } "
        + "UNION { ?system geo:location ?loc. ?loc ?x7 ?y7 } "
        + "UNION { "
          + "GRAPH <urn:semiot:graphs:private> { "
            + "?system ssncom:hasCommunicationEndpoint  ?wamp . "
            + "?system ?x8 ?y8. ?wamp ?x9 ?y9} } }",
      // @formatter:on
      SSN.class, SSNCOM.class, SEMIOT.class, GEO.class, Proto.class);

  protected static final String QUERY_UPDATE_STATE_SYSTEM =
      NamespaceUtils.newSPARQLQuery("DELETE { <${URI_SYSTEM}> saref:hasState ?x } "
          + "INSERT { <${URI_SYSTEM}> saref:hasState <${STATE}> } "
          + "WHERE { <${URI_SYSTEM}> saref:hasState ?x }", SAREF.class);
  private static final String GET_SYSTEM_URI =
      NamespaceUtils.newSPARQLQuery("SELECT ?uri {?uri a ssn:System} LIMIT 1", SSN.class);
  private static final String GET_SENSOR = NamespaceUtils.newSPARQLQuery(
      "SELECT ?sensor_uri ?sensor_id {<${URI_SYSTEM}> ssn:hasSubSystem ?sensor_uri. "
          + "?sensor_uri a ssn:SensingDevice. ?sensor_uri dcterms:identifier ?sensor_id}",
      SSN.class, DCTerms.class);
  private static final String GET_DRIVER_PID_BY_SYSTEM_ID = NamespaceUtils.newSPARQLQuery(
      "SELECT ?pid {" + "?system dcterms:identifier \"${SYSTEM_ID}\" ."
          + "GRAPH <urn:semiot:graphs:private> {" + "?system semiot:hasDriver ?pid" + "}} LIMIT 1",
      DCTerms.class, SEMIOT.class);

  private final RDFStore store;

  public DirectoryService(RDFStore store) {
    this.store = store;
  }

  public void inactiveDevice(String message) {
    String request = null;
    try {
      Model description = ModelFactory.createDefaultModel().read(new StringReader(message), null,
          RDFLanguages.TURTLE.getName());

      if (!description.isEmpty()) {
        logger.info("Update " + message);

        QueryExecution qe = QueryExecutionFactory.create(GET_SYSTEM_URI, description);
        ResultSet systems = qe.execSelect();

        while (systems.hasNext()) {
          QuerySolution qs = systems.next();
          String uriSystem = qs.getResource("uri_system").getURI();
          String state = qs.getResource("state").getURI();
          request = QUERY_UPDATE_STATE_SYSTEM.replace("${URI_SYSTEM}", uriSystem)
              .replace("${STATE}", state);
          if (uriSystem != null && state != null) {
            store.update(request);
          }
        }
      } else {
        logger.warn("Received an empty message or in a wrong format!");
      }
    } catch (RiotException ex) {
      logger.warn(ex.getMessage(), ex);
    } catch (Exception ex) {
      logger.info("Exception with string: "
          + ((request != null && !request.isEmpty()) ? request : "request message is empty"));
      logger.error(ex.getMessage(), ex);
    }
  }

  public void addDevicePrototype(URI uri) {
    try {
      Model model = RDFDataMgr.loadModel(uri.toASCIIString(), RDFLanguages.TURTLE);

      store.save(model);
    } catch (Throwable ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  /**
   * Doesn't check whether device already exists.
   *
   * @return true if the given device successfully added.
   */
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
                TOPIC_ACTUATIONS.replace("${SYSTEM_ID}", device.getId()));
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

  public void removeDataOfDriver(String pid) {
    store.update(QUERY_DELETE_ALL_DATA_DRIVER.replace("${PID}", pid));
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
