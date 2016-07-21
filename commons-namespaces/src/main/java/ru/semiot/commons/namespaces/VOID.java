package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;

public class VOID extends Namespace {
    
    public static final String URI = "http://rdfs.org/ns/void#";
    
    public static final Property classPartition = property(URI, "classPartition");
    public static final Property propertyPartition = property(URI, "propertyPartition");
    public static final Property clazz = property(URI, "class");
}
