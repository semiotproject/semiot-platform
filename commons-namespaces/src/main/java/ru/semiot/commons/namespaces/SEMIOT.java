package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class SEMIOT extends Namespace {

    public static final String URI = "http://w3id.org/semiot/ontologies/semiot#";

    public static final Resource Command = resource(URI, "Command");
    public static final Resource Actuation = resource(URI, "Actuation");

    public static final Property hasDriver = property(URI, "hasDriver");
    public static final Property hasValue = property(URI, "hasValue");
    public static final Property hasProcessParameterValue = property(URI, "hasProcessParameterValue");
    public static final Property targetOperation = property(URI, "targetOperation");
    public static final Property forProperty = property(URI, "forProperty");

}
