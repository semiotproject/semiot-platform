package ru.semiot.platform.deviceproxyservice.api.manager;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import ru.semiot.commons.namespaces.NamespaceUtils;
import ru.semiot.commons.namespaces.SEMIOT;
import ru.semiot.platform.deviceproxyservice.api.drivers.Command;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceProperties;
import ru.semiot.platform.deviceproxyservice.api.drivers.RDFTemplate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandFactory {

  private static final Pattern PATTERN_URI_PREFIX =
      Pattern.compile("@prefix\\s+(\\w*):\\s+<([^\\s]+)>\\s+\\.");
  private static final Pattern PATTERN_TAG = Pattern.compile("\\{\\{([\\w\\.]*)\\}\\}");

  public static String extractCommandId(Model model) {
    Resource commandResource = model.listSubjectsWithProperty(SEMIOT.forProcess).next();
    Literal commandId = model.listObjectsOfProperty(
        commandResource, DCTerms.identifier).next().asLiteral();

    return commandId.getString();
  }

  public static Command buildCommand(Model model, RDFTemplate template) {
    Map<String, Object> properties = deannotation(model, template);

    String commandId = extractCommandId(model);
    String processId = NamespaceUtils.extractLocalName(
        properties.get(DeviceProperties.PROCESS_URI).toString());
    Command command = new Command(processId, commandId);

    for (String key : properties.keySet()) {
      command.add(key, properties.get(key));
    }

    if (properties.containsKey(DeviceProperties.DEVICE_URI)) {
      command.add(DeviceProperties.DEVICE_ID,
          NamespaceUtils.extractLocalName(properties.get(DeviceProperties.DEVICE_URI).toString()));
    }
    return command;
  }

  private static Map<String, Object> deannotation(Model model, RDFTemplate template) {
    Map<String, Object> properties = new HashMap<>();
    Query query = buildSPARQLQuery(template);

    ResultSet resultSet = QueryExecutionFactory.create(query, model).execSelect();
    if (resultSet.hasNext()) {
      QuerySolution qs = resultSet.next();
      Iterator<String> varNames = qs.varNames();
      while (varNames.hasNext()) {
        String name = varNames.next();
        String tag = name.replaceAll("_", ".");
        RDFNode node = qs.get(name);
        if (node != null) {
          if (node.isURIResource()) {
            properties.put(tag, node.asResource().getURI());
          } else {
            properties.put(tag, node.asLiteral().getLexicalForm());
          }
        }
      }

      if (resultSet.hasNext()) {
        throw new IllegalStateException("1");
      }
    } else {
      throw new IllegalStateException("0");
    }
    return properties;
  }

  private static Query buildSPARQLQuery(RDFTemplate template) {
    String templateString = template.getTemplateString();
    StringBuilder builder = new StringBuilder();
    builder.append(extractPrefixURIs(template))
        .append("\n")
        .append("SELECT * {\n");
    templateString = templateString.replaceAll(PATTERN_URI_PREFIX.pattern(), "");
    templateString = convertTagsToVars(templateString);

    builder.append(templateString);

    builder.append("\n}");
    System.out.println(builder.toString());
    return QueryFactory.create(builder.toString());
  }

  private static String extractPrefixURIs(RDFTemplate template) {
    StringBuilder builder = new StringBuilder();
    Matcher matcher = PATTERN_URI_PREFIX.matcher(template.getTemplateString());
    while (matcher.find()) {
      String prefix = matcher.group(1);
      String uri = matcher.group(2);
      builder.append("PREFIX ").append(prefix).append(": <").append(uri).append(">\n");
    }

    return builder.toString();
  }

  private static String convertTagsToVars(String templateString) {
    Matcher matcher = PATTERN_TAG.matcher(templateString);
    while (matcher.find()) {
      String tag = matcher.group(1);
      String var = "?" + tag.replaceAll("\\.", "_");
      templateString = templateString.replaceFirst(
          "[<\\\"]{1}+\\{\\{" + tag + "\\}\\}(>|\\\"(\\^\\^[\\w:]+)*)+", var);
    }
    return templateString;
  }

}
