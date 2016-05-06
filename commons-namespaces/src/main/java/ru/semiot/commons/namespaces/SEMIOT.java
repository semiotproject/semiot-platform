package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class SEMIOT extends Namespace {

  public static final String URI = "http://w3id.org/semiot/ontologies/semiot#";

  public static final Resource Command = resource(URI, "Command");
  public static final Resource CommandResult = resource(URI, "CommandResult");

  public static final Property hasProcessParameterValue = property(URI, "hasProcessParameterValue");
  public static final Property targetProcess = property(URI, "targetProcess");
  public static final Property forParameter = property(URI, "forParameter");
  public static final Property isResultOf = property(URI, "isResultOf");

  //TODO: Remove?
  public static final Property hasDriver = property(URI, "hasDriver");

}
