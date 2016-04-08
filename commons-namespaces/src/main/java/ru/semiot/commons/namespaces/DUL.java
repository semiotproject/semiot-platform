package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;

public class DUL extends Namespace {

    public static final String URI = "http://www.loa-cnr.it/ontologies/DUL.owl#";

    public static final Property involvesAgent = property(URI, "involvesAgent");
}
