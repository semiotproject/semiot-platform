package ru.semiot.platform.apigateway.utils;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDFBase;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RDFUtils {

  public static String toString(Model model, Lang lang) {
    StringWriter writer = new StringWriter();
    model.write(writer, lang.getName());
    return writer.toString();
  }

  public static Model toModel(String rdf, Lang lang) {
    StringReader reader = new StringReader(rdf);
    return ModelFactory.createDefaultModel().read(reader, null, lang.getName());
  }

  public static boolean match(String turtle, Node subject, Node predicate, Node object) {
    MatchSinkRDF matcher = new MatchSinkRDF(subject, predicate, object);
    RDFDataMgr.parse(matcher,
        new StringReader(turtle), Lang.TURTLE);

    return matcher.match;
  }

  public static Literal toLiteral(Object value) {

    if (value instanceof String) {
      return ResourceFactory.createPlainLiteral(value.toString());
    }
    if (value instanceof Double) {
      return ResourceFactory.createTypedLiteral(value.toString(),
          XSDDatatype.XSDdouble);
    }

    throw new IllegalArgumentException();
  }

  public static List<Resource> listResourcesWithProperty(Model model, Property p,
      RDFNode... objects) {
    Map<Resource, Integer> counts = new HashMap<>();
    for (RDFNode object : objects) {
      List<Resource> resources = model.listResourcesWithProperty(p, object).toList();
      resources.stream().forEach((resource) -> {
        if (counts.containsKey(resource)) {
          counts.put(resource, counts.get(resource) + 1);
        } else {
          counts.put(resource, 1);
        }
      });
    }

    return Arrays.asList(counts.keySet().stream().filter(
        (resource) -> (counts.get(resource) == objects.length)).toArray(Resource[]::new));
  }

  public static Resource subjectWithProperty(Model model, Property property, RDFNode object) {
    return model.listSubjectsWithProperty(property, object).next();
  }

  private static class MatchSinkRDF extends StreamRDFBase {

    private final Node subject;
    private final Node predicate;
    private final Node object;
    private boolean match = false;

    public MatchSinkRDF(Node subject, Node predicate, Node object) {
      this.subject = subject;
      this.predicate = predicate;
      this.object = object;
    }

    @Override
    public void triple(Triple triple) {
      boolean subjectMatch = subject != Node.ANY ? triple.subjectMatches(subject) : true;
      boolean predicateMatch = predicate != Node.ANY ? triple.predicateMatches(predicate) : true;
      boolean objectMatch = object != Node.ANY ? triple.objectMatches(object) : true;

      if (subjectMatch && predicateMatch && objectMatch) {
        match = true;
      }
    }

    public boolean matches() {
      return match;
    }

  }

}
