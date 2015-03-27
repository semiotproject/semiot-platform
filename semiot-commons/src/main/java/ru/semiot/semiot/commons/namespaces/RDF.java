package ru.semiot.semiot.commons.namespaces;

import com.hp.hpl.jena.rdf.model.Property;

public class RDF extends Namespace {

    public static final String URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    
    public static final Property type = property(URI, "type");
    
}
