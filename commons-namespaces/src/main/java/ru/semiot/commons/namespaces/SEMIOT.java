package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;

public class SEMIOT extends Namespace {

	public static final String URI = "http://w3id.org/semiot/ontologies/semiot#";
	
	public static final Property hasDriver = property(URI, "hasDriver");
	
}
