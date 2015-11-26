package ru.semiot.semiot.commons.namespaces;

import com.hp.hpl.jena.rdf.model.Property;

public class DCTERMS extends Namespace {
    
    public static final String URI = "http://purl.org/dc/terms/#";
    
    public static final Property identifier = property(URI, "identifier");
    
}
