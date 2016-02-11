package ru.semiot.platform.apigateway.ns;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class Hydra {

    public static final String NS = "http://www.w3.org/ns/hydra/core#";
    
    public static final Resource ApiDocumentation = 
            ResourceFactory.createResource(NS + "ApiDocumentation");
    public static final Resource Class = 
            ResourceFactory.createResource(NS + "Class");
    public static final Resource Collection = 
            ResourceFactory.createResource(NS + "Collection");
    
    public static final Property supportedClass = 
            ResourceFactory.createProperty(NS, "supportedClass");
    public static final Property supportedProperty = 
            ResourceFactory.createProperty(NS, "supportedProperty");
    public static final Property member = 
            ResourceFactory.createProperty(NS, "member");
    
}
