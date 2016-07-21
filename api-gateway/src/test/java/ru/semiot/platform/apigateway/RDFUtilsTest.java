package ru.semiot.platform.apigateway;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;
import ru.semiot.commons.namespaces.Proto;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.platform.apigateway.utils.RDFUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RDFUtilsTest {

  private static final String PATH_PREFIX = "/RDFUtilsTest/";
  private final String message;

  public RDFUtilsTest() throws IOException {
    this.message = IOUtils.toString(
        this.getClass().getResourceAsStream(PATH_PREFIX + "message.ttl"));
  }

  @Test
  public void matchAny() throws IOException {
    assertTrue(RDFUtils.match(message, Node.ANY, Node.ANY, Node.ANY));
  }

  @Test
  public void matchPredicateAndObject() throws IOException {
    assertTrue(RDFUtils.match(message,
        Node.ANY,
        ResourceFactory.createProperty("http://purl.oclc.org/NET/ssnx/ssn#observedBy").asNode(),
        ResourceFactory.createResource("http://localhost/sensors/327956808-1").asNode()));

    assertFalse(RDFUtils.match(message,
        Node.ANY,
        ResourceFactory.createProperty("http://purl.oclc.org/NET/ssnx/ssn#observedBy").asNode(),
        ResourceFactory.createResource("http://localhost/sensors/327956808-2").asNode()));
  }

  @Test
  public void listResourcesWithProperty() {
    Model model = ModelFactory.createDefaultModel();
    RDFDataMgr.read(model, this.getClass().getResourceAsStream(PATH_PREFIX + "system.ttl"),
        RDFLanguages.TURTLE);

    List<Resource> actual = RDFUtils.listResourcesWithProperty(
        model, RDF.type, SSN.System, Proto.Individual);

    List<Resource> expected = new ArrayList<>();
    expected.add(ResourceFactory.createResource("http://localhost/systems/3503522021"));

    assertEquals(expected, actual);

    assertEquals(0, RDFUtils.listResourcesWithProperty(
        model, RDF.type, OWL.Class).size());
  }

}
