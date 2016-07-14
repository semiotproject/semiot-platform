package ru.semiot.platform.deviceproxyservice.api;

import static org.junit.Assert.assertEquals;

import com.github.mustachejava.MustacheException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.junit.Test;
import ru.semiot.platform.deviceproxyservice.api.drivers.Command;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceProperties;
import ru.semiot.platform.deviceproxyservice.api.drivers.RDFTemplate;
import ru.semiot.platform.deviceproxyservice.api.manager.CommandFactory;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class RDFTemplateTest {

  @Test
  public void test() {
    Map<String, Object> scope = new HashMap<>();
    scope.put("firstname", "John");
    scope.put("lastname", "Smith");

    RDFTemplate template = new RDFTemplate("test", "{{firstname}} {{lastname}}");
    String actual = template.resolveToString(scope);

    assertEquals("John Smith", actual);
  }

  @Test
  public void testMultiline() {
    Map<String, Object> scope = new HashMap<>();
    scope.put("firstname", "John");
    scope.put("lastname", "Smith");

    RDFTemplate template = new RDFTemplate("test", "{{firstname}} \n\n {{lastname}}");
    String actual = template.resolveToString(scope);

    assertEquals("John \n\n Smith", actual);
  }

  @Test
  public void testNested() {
    Map<String, Object> scope = new HashMap<>();
    scope.put("fullname", "{{firstname}} {{lastname}}");
    scope.put("firstname", "John");
    scope.put("lastname", "Smith");

    RDFTemplate template = new RDFTemplate("test", "{{fullname}}");
    String actual = template.resolveToString(scope);

    assertEquals("John Smith", actual);
  }

  @Test(expected = MustacheException.class)
  public void testMissing() {
    Map<String, Object> scope = new HashMap<>();
    scope.put("firstname", "John");

    RDFTemplate template = new RDFTemplate("test", "{{firstname}} {{lastname}}");

    String actual = template.resolveToString(scope);
  }

  @Test(expected = MustacheException.class)
  public void testMissingNested() {
    Map<String, Object> scope = new HashMap<>();
    scope.put("fullname", "{{firstname}} {{lastname}}");
    scope.put("firstname", "John");

    RDFTemplate template = new RDFTemplate("test", "{{fullname}}");

    String actual = template.resolveToString(scope);
  }

  @Test
  public void testBuildCommandFromModelAndRDFTemplate_1() {
    RDFTemplate template = new RDFTemplate("test",
        "@prefix dul: <http://www.loa-cnr.it/ontologies/DUL.owl#> ." +
            "@prefix semiot: <http://w3id.org/semiot/ontologies/semiot#> ." +
            "@prefix dcterms: <http://purl.org/dc/terms/> ." +
            "@prefix : <https://raw.githubusercontent.com/semiotproject/semiot-platform/" +
            "master/device-proxy-service-drivers/mock-plain-lamp/src/main/resources/" +
            "ru/semiot/drivers/mocks/plainlamp/prototype.ttl#> ." +
            "[ a semiot:StopCommand ;" +
            " dcterms:identifier \"light-stopcommand\" ;" +
            " semiot:forProcess <{{ru.semiot.platform.process.uri}}> ;" +
            " dul:associatedWith <{{ru.semiot.platform.device.uri}}>" +
            "] .");
    Model model = ModelFactory.createDefaultModel().read(new StringReader(
        "@prefix dul: <http://www.loa-cnr.it/ontologies/DUL.owl#> ." +
            "@prefix semiot: <http://w3id.org/semiot/ontologies/semiot#> ." +
            "@prefix dcterms: <http://purl.org/dc/terms/> ." +
            "@prefix : <https://raw.githubusercontent.com/semiotproject/semiot-platform/" +
            "master/device-proxy-service-drivers/mock-plain-lamp/src/main/resources/" +
            "ru/semiot/drivers/mocks/plainlamp/prototype.ttl#> ." +
            "[ a semiot:StopCommand ;" +
            " dcterms:identifier \"light-stopcommand\" ;" +
            " semiot:forProcess <http://localhost/systems/01/processes/light> ;" +
            " dul:associatedWith <http://localhost/systems/01>" +
            "] ."), null, RDFLanguages.strLangTurtle);
    Command command = CommandFactory.buildCommand(model, template);

    for (String key : command.getAll().keySet()) {
      System.out.println(key + " " + command.get(key));
    }

    Map<String, Object> properties = command.getAll();
    assertEquals(5, properties.size());

    assertEquals("light-stopcommand", properties.get(DeviceProperties.COMMAND_ID));
    assertEquals("light", properties.get(DeviceProperties.PROCESS_ID));
    assertEquals("01", properties.get(DeviceProperties.DEVICE_ID));
  }

  @Test
  public void testBuildCommandFromModelAndRDFTemplate_2() {
    RDFTemplate template = new RDFTemplate("test",
        "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ." +
            "@prefix dul: <http://www.loa-cnr.it/ontologies/DUL.owl#> ." +
            "@prefix dcterms: <http://purl.org/dc/terms/> ." +
            "@prefix semiot: <http://w3id.org/semiot/ontologies/semiot#> ." +
            "@prefix sh: <http://www.w3.org/ns/shacl#> ." +
            "@prefix : <https://raw.githubusercontent.com/semiotproject/semiot-platform/" +
            "master/device-proxy-service-drivers/mock-plain-lamp/src/main/resources/" +
            "ru/semiot/drivers/mocks/plainlamp/prototype.ttl#> ." +
            "[ a semiot:StartCommand ;" +
            "  dcterms:identifier \"light-startcommand\" ;" +
            "  semiot:forProcess <{{ru.semiot.platform.process.uri}}> ;" +
            "  dul:associatedWith <{{ru.semiot.platform.device.uri}}> ;" +
            "  dul:hasParameter [" +
            "    a semiot:MappingParameter ;" +
            "    semiot:forParameter :PlainLamp-Shine-Lumen ;" +
            "    dul:hasParameterDataValue \"{{ru.semiot.drivers.mocks.plainlamp.light.lumen}}\"^^xsd:integer ;" +
            "  ] ;" +
            "  dul:hasParameter [" +
            "    a semiot:MappingParameter ;" +
            "    semiot:forParameter :PlainLamp-Shine-Color ;" +
            "    dul:hasParameterDataValue \"{{ru.semiot.drivers.mocks.plainlamp.light.color}}\"^^xsd:integer ;" +
            "  ] ;" +
            "] .");
    Model model = ModelFactory.createDefaultModel().read(new StringReader(
        "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ." +
            "@prefix dul: <http://www.loa-cnr.it/ontologies/DUL.owl#> ." +
            "@prefix dcterms: <http://purl.org/dc/terms/> ." +
            "@prefix semiot: <http://w3id.org/semiot/ontologies/semiot#> ." +
            "@prefix sh: <http://www.w3.org/ns/shacl#> ." +
            "@prefix : <https://raw.githubusercontent.com/semiotproject/semiot-platform/" +
            "master/device-proxy-service-drivers/mock-plain-lamp/src/main/resources/" +
            "ru/semiot/drivers/mocks/plainlamp/prototype.ttl#> ." +
            "[ a semiot:StartCommand ;" +
            "  dcterms:identifier \"light-startcommand\" ;" +
            "  semiot:forProcess <http://localhost/systems/01/processes/light> ;" +
            "  dul:associatedWith <http://localhost/systems/01> ;" +
            "  dul:hasParameter [" +
            "    a semiot:MappingParameter ;" +
            "    semiot:forParameter :PlainLamp-Shine-Lumen ;" +
            "    dul:hasParameterDataValue \"880\"^^xsd:integer ;" +
            "  ] ;" +
            "  dul:hasParameter [" +
            "    a semiot:MappingParameter ;" +
            "    semiot:forParameter :PlainLamp-Shine-Color ;" +
            "    dul:hasParameterDataValue \"3900\"^^xsd:integer ;" +
            "  ] ;" +
            "] ."), null, RDFLanguages.strLangTurtle);
    Command command = CommandFactory.buildCommand(model, template);

    for (String key : command.getAll().keySet()) {
      System.out.println(key + " " + command.get(key));
    }
    Map<String, Object> properties = command.getAll();
    assertEquals(7, properties.size());

    assertEquals("light-startcommand", properties.get(DeviceProperties.COMMAND_ID));
    assertEquals("light", properties.get(DeviceProperties.PROCESS_ID));
    assertEquals("01", properties.get(DeviceProperties.DEVICE_ID));
  }

}
