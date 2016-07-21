package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;

public class SHACL extends Namespace {
    
    public static final String PREFIX = "sh";
    public static final String URI = "http://www.w3.org/ns/shacl#";
    
    public static final Property clazz = property(URI, "class");
    
}
