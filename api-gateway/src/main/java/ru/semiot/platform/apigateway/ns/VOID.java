package ru.semiot.platform.apigateway.ns;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public abstract class VOID {
    public static final String NS = "http://rdfs.org/ns/void#";
    
    public static final Property classPartition = ResourceFactory
            .createProperty(NS, "classPartition");
    public static final Property clazz = ResourceFactory.createProperty(NS, "class");
}
