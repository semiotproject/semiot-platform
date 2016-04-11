package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Resource;

public class EMTR extends Namespace {
    
    public static final String URI = "http://purl.org/NET/ssnext/electricmeters#";
    
    public static final Resource ElectricMeter = resource(URI, "ElectricMeter");
    
}
