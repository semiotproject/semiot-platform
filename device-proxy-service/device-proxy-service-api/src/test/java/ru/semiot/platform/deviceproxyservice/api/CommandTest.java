package ru.semiot.platform.deviceproxyservice.api;

import static org.junit.Assert.assertTrue;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.junit.Test;
import ru.semiot.platform.deviceproxyservice.api.drivers.Command;
import ru.semiot.platform.deviceproxyservice.api.drivers.Configuration;
import ru.semiot.platform.deviceproxyservice.api.drivers.Keys;

import java.io.IOException;
import java.io.StringWriter;

public class CommandTest {

  @Test
  public void toRDFAsModel() throws IOException {
    StringWriter writer1 = new StringWriter();
    Configuration configuration = new Configuration();
    configuration.put(Keys.PLATFORM_SYSTEMS_URI_PREFIX, "http://localhost/systems/");

    Model expected = ModelFactory.createDefaultModel().read(
        this.getClass().getResourceAsStream("/ru/semiot/platform/deviceproxyservice/api/command.ttl"),
        null,
        RDFLanguages.strLangTurtle);
    expected.write(writer1, RDFLanguages.strLangTurtle);
    System.out.println("Expected:");
    System.out.println(writer1.toString());

    Command command = new Command(expected);
    Model actual = command.toRDFAsModel(configuration);

    StringWriter writer2 = new StringWriter();
    actual.write(writer2, RDFLanguages.strLangTurtle);
    System.out.println("Actual:");
    System.out.println(writer2.toString());

    Model difference = expected.intersection(actual);
    StringWriter writer3 = new StringWriter();
    difference.write(writer3, RDFLanguages.strLangTurtle);
    System.out.println("Difference:");
    System.out.println(writer3.toString());

    assertTrue(difference.isEmpty());
  }
}
