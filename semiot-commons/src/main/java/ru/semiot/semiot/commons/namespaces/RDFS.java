package ru.semiot.semiot.commons.namespaces;

import com.hp.hpl.jena.rdf.model.Property;

public class RDFS extends Namespace {
    
    public static final String URI = "http://www.w3.org/2000/01/rdf-schema#";
    
    public static final Property subClassOf = property(URI, "subClassOf");
    
}
