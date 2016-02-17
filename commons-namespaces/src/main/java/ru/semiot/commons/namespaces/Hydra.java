package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class Hydra extends Namespace {

    public static final String URI = "http://www.w3.org/ns/hydra/core#";
    
    public static final Resource ApiDocumentation = resource(URI, "ApiDocumentation");
    public static final Resource Class = resource(URI, "Class");
    public static final Resource Collection = resource(URI, "Collection");
    
    public static final Property supportedClass = property(URI, "supportedClass");
    public static final Property supportedProperty = property(URI, "supportedProperty");
    public static final Property member = property(URI, "member");
    
}
