package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;

public class RDF extends Namespace {

    public static final String URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    
    public static final Property type = property(URI, "type");
    
}
