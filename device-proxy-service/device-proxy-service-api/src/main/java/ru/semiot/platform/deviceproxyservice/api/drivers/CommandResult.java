package ru.semiot.platform.deviceproxyservice.api.drivers;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.SEMIOT;

import java.io.IOException;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class CommandResult {

  private static final Logger logger = LoggerFactory.getLogger(CommandResult.class);
  private static RDFTemplate TEMPLATE_COMMANDRESULT;

  static {
    try {
      CommandResult.TEMPLATE_COMMANDRESULT =
          new RDFTemplate("commandresult", CommandResult.class.getResourceAsStream(
              "/ru/semiot/platform/deviceproxyservice/api/CommandResult.ttl"));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private final Map<String, Object> properties = new HashMap<>();
  private final RDFTemplate commandTemplate;

  public CommandResult(Command command, RDFTemplate template, ZonedDateTime dateTime) {
    this.properties.putAll(command.getAll());
    this.properties.put(DeviceProperties.DEVICE_URI,
        "{{" + Keys.PLATFORM_SYSTEMS_URI_PREFIX + "}}/" + command.get(DeviceProperties.DEVICE_ID));
    this.properties.put(DeviceProperties.COMMANDRESULT_DATETIME,
        dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

    this.commandTemplate = template;
  }

  public String get(String name) {
    return properties.get(name).toString();
  }

  public Model toRDFAsModel(Map<String, Object> configuration) {
    logger.debug(TEMPLATE_COMMANDRESULT.resolveToString(properties, configuration));
    Model result = ModelFactory.createDefaultModel().read(
        TEMPLATE_COMMANDRESULT.resolveToReader(properties, configuration),
        null,
        TEMPLATE_COMMANDRESULT.getRDFLanguage());
    logger.debug(commandTemplate.resolveToString(properties, configuration));
    Model command = ModelFactory.createDefaultModel().read(
        commandTemplate.resolveToReader(properties, configuration),
        null,
        commandTemplate.getRDFLanguage());
    result.add(command);

    Resource resourceCommandResult = result.listResourcesWithProperty(
        RDF.type, SEMIOT.CommandResult).next();
    Resource resourceCommand = result.listResourcesWithProperty(SEMIOT.forProcess).next();

    result.add(resourceCommandResult, SEMIOT.isResultOf, resourceCommand);

    return result;
  }

  public String toRDFAsString(Configuration configuration, Lang lang) {
    StringWriter writer = new StringWriter();
    toRDFAsModel(configuration).write(writer, lang.getName());

    return writer.toString();
  }
}
