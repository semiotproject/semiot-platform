package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;

public class SAREF extends Namespace {
    
    public static final String URI = "http://ontology.tno.nl/saref#";
    
    public static final Property hasState = property(URI, "hasState");
    
}
