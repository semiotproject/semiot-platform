package ru.semiot.platform.deviceproxyservice.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;
import ru.semiot.commons.namespaces.DUL;
import ru.semiot.commons.namespaces.NamespaceUtils;
import ru.semiot.commons.namespaces.SEMIOT;
import ru.semiot.platform.deviceproxyservice.api.drivers.Command;
import ru.semiot.platform.deviceproxyservice.api.drivers.Configuration;
import ru.semiot.platform.deviceproxyservice.api.drivers.Keys;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

public class CommandTest {

  private static final String RESOURCES_PATH = "/ru/semiot/platform/deviceproxyservice/api/";

  @Test
  public void toRDFAsModel() throws IOException {
//    StringWriter writer1 = new StringWriter();
//    Configuration configuration = new Configuration();
//    configuration.put(Keys.PLATFORM_SYSTEMS_URI_PREFIX, "http://localhost/systems/");
//
//    Model expected = ModelFactory.createDefaultModel().read(
//        this.getClass().getResourceAsStream("/ru/semiot/platform/deviceproxyservice/api/command.ttl"),
//        null,
//        RDFLanguages.strLangTurtle);
//
//    expected.write(writer1, RDFLanguages.strLangTurtle);
//    System.out.println("Expected:");
//    System.out.println(writer1.toString());
//
//    Command command = new Command(expected);
//    Model actual = command.toRDFAsModel(configuration);
//
//    StringWriter writer2 = new StringWriter();
//    actual.write(writer2, RDFLanguages.strLangTurtle);
//    System.out.println("Actual:");
//    System.out.println(writer2.toString());
//
//    Model difference = expected.intersection(actual);
//    StringWriter writer3 = new StringWriter();
//    difference.write(writer3, RDFLanguages.strLangTurtle);
//    System.out.println("Difference:");
//    System.out.println(writer3.toString());
//
//    assertTrue(difference.isEmpty());
  }

  @Test
  public void getCommandType() {
//    StringWriter writer1 = new StringWriter();
//    Configuration configuration = new Configuration();
//    configuration.put(Keys.PLATFORM_SYSTEMS_URI_PREFIX, "http://localhost/systems/");
//
//    Model expected = ModelFactory.createDefaultModel().read(
//        this.getClass().getResourceAsStream("/ru/semiot/platform/deviceproxyservice/api/command.ttl"),
//        null,
//        RDFLanguages.strLangTurtle);
//
//    expected.write(writer1, RDFLanguages.strLangTurtle);
//    System.out.println("Command:");
//    System.out.println(writer1.toString());
//    Command command = new Command(expected);
//
//    assertEquals("http://w3id.org/semiot/ontologies/semiot#StartCommand", command.getCommandType());

  }

  @Test
  public void testDeannotation() {
//    Model description = ModelFactory.createDefaultModel().read(
//        this.getClass().getResourceAsStream(RESOURCES_PATH + "command.ttl"),
//        null,
//        RDFLanguages.strLangTurtle);
//
//    Query query = QueryFactory.create(NamespaceUtils.newSPARQLQuery(
//        "SELECT * {" +
//            "?b1 semiot:forProcess ?process_uri ." +
//            "?b1 dul:associatedWith ?device_uri ." +
//            "?b2 dul:hasParameterDataValue ?parameter_lumen_value ." +
//            "?b2 dul:hasParameterDataValue ?parameter_color_value ." +
//            "}", RDF.class, DUL.class, SEMIOT.class));
//
//    ResultSet rs = QueryExecutionFactory.create(query, description).execSelect();
//
//    assertTrue(rs.hasNext());
//
//    while (rs.hasNext()) {
//      QuerySolution qs = rs.next();
//      Iterator<String> names = qs.varNames();
//      System.out.println("Row:");
//      while (names.hasNext()) {
//        String name = names.next();
//        System.out.println(name + " " + qs.get(name));
//      }
//    }
  }
}
