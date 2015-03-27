package ru.semiot.semiot.commons.namespaces;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public abstract class Namespace {
    
    protected static final Resource resource(String base, String name) {
        return ResourceFactory.createResource(base + name);
    }

    protected static final Property property(String base, String name) {
        return ResourceFactory.createProperty(base + name);
    }
}
