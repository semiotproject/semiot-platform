package ru.semiot.platform.apigateway.rest;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import ru.semiot.platform.apigateway.utils.URIUtils;

import java.net.URI;

public class ResourceUtils {

  public static final Resource createResourceFromClass(URI root, String className) {
    return ResourceUtils.createResourceFromClass(URIUtils.extractRootURL(root), className);
  }

  public static final Resource createResourceFromClass(String rootURL, String className) {
    StringBuilder builder = new StringBuilder(rootURL)
        .append("/doc#")
        .append(className);

    return ResourceFactory.createResource(builder.toString());
  }

}
