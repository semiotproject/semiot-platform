package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;

public class DCTERMS extends Namespace {
    
    public static final String URI = "http://purl.org/dc/terms/#";
    
    public static final Property identifier = property(URI, "identifier");
    
}
