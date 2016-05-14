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

public class CommandResult {

  private static final Logger logger = LoggerFactory.getLogger(CommandResult.class);
  private static String COMMANDRESULT_TEMPLATE;

  static {
    try {
      CommandResult.COMMANDRESULT_TEMPLATE = IOUtils.toString(
          CommandResult.class.getResourceAsStream(
              "/ru/semiot/platform/deviceproxyservice/api/CommandResult.ttl"));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private final Map<String, String> properties = new HashMap<>();
  private final Command command;

  public CommandResult(Command command, ZonedDateTime dateTime) {
    this.command = command;

    properties.put(DeviceProperties.DEVICE_ID, command.getDeviceId());
    properties.put(DeviceProperties.COMMANDRESULT_DATETIME,
        dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
  }

  public Command getCommand() {
    return command;
  }

  public Model toRDFAsModel(Map<String, String> configuration) {
    StringReader readerCommandResult = new StringReader(TemplateUtils.resolve(
        COMMANDRESULT_TEMPLATE, properties, configuration));
    Model model = ModelFactory
        .createDefaultModel()
        .read(readerCommandResult, null, RDFLanguages.strLangTurtle);
    model.add(command.toRDFAsModel(configuration));

    Resource resourceCommandResult = model.listResourcesWithProperty(
        RDF.type, SEMIOT.CommandResult).next();
    Resource resourceCommand = model.listResourcesWithProperty(
        RDF.type, SEMIOT.Command).next();

    model.add(resourceCommandResult, SEMIOT.isResultOf, resourceCommand);

    return model;
  }

  public String toRDFAsString(Configuration configuration, Lang lang) {
    StringWriter writer = new StringWriter();
    toRDFAsModel(configuration).write(writer, lang.getName());

    return writer.toString();
  }
}
