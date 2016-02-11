package ru.semiot.platform.apigateway.ns;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class Proto {

    public static final String NS = "http://w3id.org/semiot/ontologies/proto#";
    
    public static final Resource Individual = ResourceFactory.createResource(NS + "Individual");
    
    public static final Property hasPrototype = ResourceFactory.createProperty(NS, "hasPrototype");
}
