package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;

public class RDFS extends Namespace {
    
    public static final String URI = "http://www.w3.org/2000/01/rdf-schema#";
    
    public static final Property subClassOf = property(URI, "subClassOf");
    
}
