package ru.semiot.services.tsdbservice.model;

import static ru.semiot.services.tsdbservice.ServiceConfig.CONFIG;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import ru.semiot.commons.namespaces.QUDT;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.services.tsdbservice.TSDBClient;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.validation.constraints.NotNull;

public class Observation {

  private static final String SENSOR_URI_TEMPLATE =
      CONFIG.rootUrl() + "systems/${SYSTEM_ID}/subsystems/${SENSOR_ID}";

  private final String sensorId;
  private final String systemId;
  private final ZonedDateTime eventTime;
  private final Resource property;
  private final String value;
  private final Resource featureOfInterest;

  public Observation(@NotNull String systemId, @NotNull String sensorId, @NotNull String eventTime,
      @NotNull String property, String featureOfInterest, @NotNull String value) {
    this.systemId = systemId;
    this.sensorId = sensorId;
    this.eventTime = ZonedDateTime.parse(eventTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    this.property = ResourceFactory.createResource(property);
    if (featureOfInterest != null) {
      this.featureOfInterest = ResourceFactory.createResource(featureOfInterest);
    } else {
      this.featureOfInterest = null;
    }
    this.value = value;
  }

  public String insert() {
    String foi = null;
    if (featureOfInterest != null) {
      foi = featureOfInterest.getURI();
    }
    return "INSERT INTO semiot.observation (sensor_id, system_id, "
        + "event_time, property, feature_of_interest, value)" + " VALUES ('" + sensorId + "', '"
        + systemId + "','" + TSDBClient.formatToCQLTimestamp(eventTime) + "', '"
        + property.getURI() + "', '" + foi + "', '" + value + "')";
  }

  public Model toRDF() {
    Model model = ModelFactory.createDefaultModel();
    Resource observation = ResourceFactory.createResource();
    Resource obsResult = ResourceFactory.createResource();
    Resource obsValue = ResourceFactory.createResource();
    Resource sensor = ResourceFactory.createResource(
        SENSOR_URI_TEMPLATE.replace("${SYSTEM_ID}", systemId).replace("${SENSOR_ID}", sensorId));

    model.add(observation, RDF.type, SSN.Observaton)
        .add(observation, SSN.observedProperty, property).add(observation, SSN.observedBy, sensor)
        .add(observation, SSN.observationResultTime,
            ResourceFactory.createTypedLiteral(eventTime.toString(), XSDDatatype.XSDdateTime));

    if (featureOfInterest != null) {
      model.add(observation, SSN.featureOfInterest, featureOfInterest);
    }

    model.add(observation, SSN.observationResult, obsResult)
        .add(obsResult, RDF.type, SSN.SensorOutput).add(obsResult, SSN.isProducedBy, sensor)
        .add(obsResult, SSN.hasValue, obsValue);

    model.add(obsValue, RDF.type, QUDT.QuantityValue).add(obsValue, QUDT.quantityValue,
        toLiteral(value));

    return model;
  }

  private Literal toLiteral(Object value) {
    if (value instanceof String) {
      return ResourceFactory.createPlainLiteral(value.toString());
    }
    if (value instanceof Double) {
      return ResourceFactory.createTypedLiteral(value.toString(), XSDDatatype.XSDdouble);
    }

    throw new IllegalArgumentException();
  }

}
