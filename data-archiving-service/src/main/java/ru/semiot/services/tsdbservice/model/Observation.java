package ru.semiot.services.tsdbservice.model;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import ru.semiot.commons.namespaces.QUDT;
import ru.semiot.commons.namespaces.SSN;

import javax.validation.constraints.NotNull;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Observation {

    private static final String SENSOR_URI_PREFIX = "http://localhost/sensors/";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ");

    private final String sensorId;
    private final String system_id;
    private final ZonedDateTime eventTime;
    private final Resource property;
    private final String value;
    private final Resource featureOfInterest;

    public Observation(@NotNull String sensorId, @NotNull String system_id,
                       @NotNull String eventTime, @NotNull String property,
                       String featureOfInterest, @NotNull String value) {
        this.sensorId = sensorId;
        this.system_id = system_id;
        this.eventTime = ZonedDateTime.parse(eventTime,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME);
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
                + "event_time, property, feature_of_interest, value)"
                + " VALUES ('" + sensorId + "', '" + system_id + "','"
                + eventTime.toString() + "', '" + property.getURI() + "', '" + foi
                + "', '" + value + "')";
    }

    public Model toRDF() {
        Model model = ModelFactory.createDefaultModel();
        Resource observation = ResourceFactory.createResource();
        Resource obsResult = ResourceFactory.createResource();
        Resource obsValue = ResourceFactory.createResource();

        model.add(observation, RDF.type, SSN.Observaton)
                .add(observation, SSN.observedProperty, property)
                .add(observation, SSN.observedBy, SENSOR_URI_PREFIX + sensorId)
                .add(observation, SSN.observationResultTime, ResourceFactory
                        .createTypedLiteral(eventTime.toString(), XSDDatatype.XSDdateTime));

        if (featureOfInterest != null) {
            model.add(observation, SSN.featureOfInterest, featureOfInterest);
        }

        model.add(observation, SSN.observationResult, obsResult)
                .add(obsResult, RDF.type, SSN.SensorOutput)
                .add(obsResult, SSN.isProducedBy, SENSOR_URI_PREFIX + sensorId)
                .add(obsResult, SSN.hasValue, obsValue);

        model.add(obsValue, RDF.type, QUDT.QuantityValue)
                .add(obsValue, QUDT.quantityValue, toLiteral(value));

        return model;
    }

    private Literal toLiteral(Object value) {
        if (value instanceof String) {
            return ResourceFactory.createPlainLiteral(value.toString());
        }
        if (value instanceof Double) {
            return ResourceFactory.createTypedLiteral(value.toString(),
                    XSDDatatype.XSDdouble);
        }

        throw new IllegalArgumentException();
    }

}
