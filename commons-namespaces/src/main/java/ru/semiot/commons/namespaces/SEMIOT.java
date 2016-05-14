package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class SEMIOT extends Namespace {

  public static final String URI = "http://w3id.org/semiot/ontologies/semiot#";

  public static final Resource Command = resource(URI, "Command");
  public static final Resource CommandResult = resource(URI, "CommandResult");
  public static final Resource Process = resource(URI, "Process");
  public static final Resource StartCommand = resource(URI, "StartCommand");
  public static final Resource StopCommand = resource(URI, "StopCommand");

  public static final Property forProcess = property(URI, "forProcess");
  public static final Property forParameter = property(URI, "forParameter");
  public static final Property isResultOf = property(URI, "isResultOf");
  public static final Property supportedProcess = property(URI, "supportedProcess");
  public static final Property supportedCommand = property(URI, "supportedCommand");

  //TODO: Remove?
  public static final Property hasDriver = property(URI, "hasDriver");

}
