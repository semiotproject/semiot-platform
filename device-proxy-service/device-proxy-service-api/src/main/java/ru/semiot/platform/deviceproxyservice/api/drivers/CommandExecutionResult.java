package ru.semiot.platform.deviceproxyservice.api.drivers;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.SEMIOT;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class CommandExecutionResult {

  private static final Logger logger = LoggerFactory.getLogger(CommandExecutionResult.class);
  private static String actuation;

  static {
    try {
      CommandExecutionResult.actuation = IOUtils.toString(
          CommandExecutionResult.class.getResourceAsStream(
              "/ru/semiot/platform/deviceproxyservice/api/Actuation.ttl"));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private final Model command;
  private final Device device;
  private final ZonedDateTime dateTime;

  public CommandExecutionResult(Device device, Model command,
                                ZonedDateTime dateTime) {
    this.command = command;
    this.device = device;
    this.dateTime = dateTime;
  }

  public Device getDevice() {
    return device;
  }

  public Model toActuationAsModel(Configuration configuration) {
    Map<String, String> resultProperties = new HashMap<>();
    resultProperties.put(DeviceProperties.ACTUATION_DATETIME,
        dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    StringReader readerActuation = new StringReader(TemplateUtils.resolve(
        actuation,
        resultProperties,
        device.getProperties(),
        configuration));
    Model model = ModelFactory.createDefaultModel().read(
        readerActuation, null, RDFLanguages.strLangTurtle);
    model.add(command);

    Resource resourceActuation = model.listResourcesWithProperty(
        RDF.type, SEMIOT.Actuation).next();
    Resource resourceCommand = model.listResourcesWithProperty(
        RDF.type, SEMIOT.Command).next();

    model.add(resourceActuation, SEMIOT.hasValue, resourceCommand);

    return model;
  }

  public String toActuationAsString(Configuration configuration, Lang lang) {
    StringWriter writer = new StringWriter();
    toActuationAsModel(configuration).write(writer, lang.getName());

    return writer.toString();
  }
}
