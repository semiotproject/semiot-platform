package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class Proto extends Namespace {

    public static final String URI = "http://w3id.org/semiot/ontologies/proto#";
    
    public static final Resource Individual = resource(URI, "Individual");
    
    public static final Property hasPrototype = property(URI, "hasPrototype");
}
