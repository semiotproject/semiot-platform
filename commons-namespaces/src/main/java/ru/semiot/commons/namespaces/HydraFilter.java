package ru.semiot.commons.namespaces;


import org.apache.jena.rdf.model.Property;

public class HydraFilter extends Namespace {

  public static final String URI = "http://w3id.org/semiot/ontologies/hydra-filter#";

  public static final Property viewOf = property(URI, "viewOf");
}
