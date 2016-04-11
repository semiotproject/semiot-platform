package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public abstract class Namespace {
    
    protected static final Resource resource(String base, String name) {
        return ResourceFactory.createResource(base + name);
    }

    protected static final Property property(String base, String name) {
        return ResourceFactory.createProperty(base + name);
    }
}
