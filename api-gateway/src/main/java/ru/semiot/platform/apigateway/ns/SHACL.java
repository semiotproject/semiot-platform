package ru.semiot.platform.apigateway.ns;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public abstract class SHACL {
    
    public static final String NS = "http://www.w3.org/ns/shacl#";
    
    public static final Property clazz = ResourceFactory.createProperty(NS, "class");
    
}
