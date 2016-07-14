package ru.semiot.commons.rdf;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class ModelUtils {

  public static List<RDFNode> objectsOfProperty(Model model, Property property, Resource subject,
      RDFNode objectToIgnore) {
    List<RDFNode> objects = new ArrayList<>();

    NodeIterator iterator = model.listObjectsOfProperty(subject, property);
    while (iterator.hasNext()) {
      RDFNode node = iterator.next();
      if (!node.equals(objectToIgnore)) {
        objects.add(node);
      }
    }

    return objects;
  }

  public static List<RDFNode> objectsOfProperty(Model model, Property property,
      RDFNode objectToIgnore) {
    List<RDFNode> objects = new ArrayList<>();

    NodeIterator iterator = model.listObjectsOfProperty(property);
    while (iterator.hasNext()) {
      RDFNode node = iterator.next();
      if (!node.equals(objectToIgnore)) {
        objects.add(node);
      }
    }

    return objects;
  }

  public static RDFNode objectOfProperty(Model model, Property property) {
    return model.listObjectsOfProperty(property).next();
  }
}
