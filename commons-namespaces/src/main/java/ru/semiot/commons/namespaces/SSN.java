package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class SSN extends Namespace {

    public static final String URI = "http://purl.oclc.org/NET/ssnx/ssn#";
    
    public static final Property hasSubsystem = property(URI, "hasSubsystem");
    public static final Property observedProperty = 
            property(URI, "observedProperty");
    public static final Property featureOfInterest =
            property(URI, "featureOfInterest");
    public static final Property observedBy = property(URI, "observedBy");
    public static final Property isProducedBy = property(URI, "isProducedBy");
    public static final Property hasValue = property(URI, "hasValue");
    public static final Property observationResult = property(URI, "observationResult");
    public static final Property observationResultTime = 
            property(URI, "observationResultTime");
    
    public static final Resource SensingDevice = resource(URI, "SensingDevice");
    public static final Resource Sensor = resource(URI, "Sensor");
    public static final Resource System = resource(URI, "System");
    public static final Resource Observaton = resource(URI, "Observation");
    public static final Resource SensorOutput = resource(URI, "SensorOutput");

}
