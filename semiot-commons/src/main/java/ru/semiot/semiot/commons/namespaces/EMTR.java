package ru.semiot.semiot.commons.namespaces;

import com.hp.hpl.jena.rdf.model.Resource;

public class EMTR extends Namespace {
    
    public static final String URI = "http://purl.org/NET/ssnext/electricmeters#";
    
    public static final Resource ElectricMeter = resource(URI, "ElectricMeter");
    
}
