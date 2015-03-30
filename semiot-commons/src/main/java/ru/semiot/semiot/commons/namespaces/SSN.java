package ru.semiot.semiot.commons.namespaces;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class SSN extends Namespace {

    public static final String URI = "http://purl.oclc.org/NET/ssnx/ssn#";
    
    public static final Property hasSubsystem = property(URI, "hasSubsystem");
    
    public static final Resource SensingDevice = resource(URI, "SensingDevice");
    public static final Resource Sensor = resource(URI, "Sensor");
    public static final Resource System = resource(URI, "System");

}
