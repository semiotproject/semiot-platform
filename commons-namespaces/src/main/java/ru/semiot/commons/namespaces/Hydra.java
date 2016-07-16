package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class Hydra extends Namespace {

  public static final String URI = "http://www.w3.org/ns/hydra/core#";

  public static final Resource ApiDocumentation = resource(URI, "ApiDocumentation");
  public static final Resource Class = resource(URI, "Class");
  public static final Resource Collection = resource(URI, "Collection");
  public static final Resource PartialCollectionView = resource(URI, "PartialCollectionView");
  public static final Resource Operation = resource(URI, "Operation");

  public static final Property expects = property(URI, "expects");
  public static final Property supportedClass = property(URI, "supportedClass");
  public static final Property supportedProperty = property(URI, "supportedProperty");
  public static final Property supportedOperation = property(URI, "supportedOperation");
  public static final Property member = property(URI, "member");
  public static final Property method = property(URI, "method");
  public static final Property property = property(URI, "property");
  public static final Property returns = property(URI, "returns");
  public static final Property next = property(URI, "next");
  public static final Property first = property(URI, "first");
  public static final Property last = property(URI, "last");
  public static final Property totalItems = property(URI, "totalItems");

}
