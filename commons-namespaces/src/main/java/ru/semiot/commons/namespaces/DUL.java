package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;

public class DUL extends Namespace {

  public static final String URI = "http://www.loa-cnr.it/ontologies/DUL.owl#";

  public static final Property involvesAgent = property(URI, "involvesAgent");
  public static final Property hasEventTime = property(URI, "hasEventTime");
  public static final Property hasParameterDataValue = property(URI, "hasParameterDataValue");
  public static final Property hasParameter = property(URI, "hasParameter");
}
