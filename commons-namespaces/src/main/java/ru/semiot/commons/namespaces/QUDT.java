package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Resource;

public class QUDT extends Namespace {
    
    public static final String URI = "http://qudt.org/schema/qudt#";
    
    public static final Resource Enumeration = resource(URI, "Enumeration");
}
