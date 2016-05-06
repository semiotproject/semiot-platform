package ru.semiot.commons.rdf;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;

public class ResourceUtils {

  public static boolean isURI(String uri) {
    if (uri.startsWith("http://") || uri.startsWith("https://")) {
      return true;
    } else {
      return false;
    }
  }

  public static RDFNode toRDFNode(String value) {
    if (isURI(value)) {
      return ResourceFactory.createResource(value);
    } else if (NumberUtils.isNumber(value)) {
      if (value.contains(".")) {
        return ResourceFactory.createTypedLiteral(value, XSDDatatype.XSDdouble);
      }
      if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
        return ResourceFactory.createTypedLiteral(value, XSDDatatype.XSDboolean);
      } else {
        return ResourceFactory.createTypedLiteral(value, XSDDatatype.XSDinteger);
      }
    } else {
      return ResourceFactory.createTypedLiteral(value, XSDDatatype.XSDstring);
    }
  }
}
