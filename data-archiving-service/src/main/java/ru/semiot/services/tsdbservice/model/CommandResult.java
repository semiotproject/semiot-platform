package ru.semiot.services.tsdbservice.model;

import static ru.semiot.services.tsdbservice.ServiceConfig.CONFIG;

import com.datastax.driver.core.UDTValue;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import ru.semiot.commons.namespaces.DUL;
import ru.semiot.commons.namespaces.SEMIOT;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

public class CommandResult {

  private static final String SENSOR_URI_PREFIX = CONFIG.sensorsURIPrefix() + "systems/";
  private final String systemId;
  private final ZonedDateTime eventTime;
  private final String type;
  private final List<CommandProperty> properties = new ArrayList<>();
  private final Map<Resource, RDFNode> parameters = new HashMap<>();

  public CommandResult(@NotNull String systemId, @NotNull String eventTime,
      @NotNull String type) {
    this.systemId = systemId;
    this.eventTime = ZonedDateTime.parse(eventTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    this.type = type;
  }

  public void addProperty(Property property, RDFNode value) {
    if (value.isLiteral() || value.isURIResource()) {
      properties.add(new CommandProperty(property, value));
    }
  }

  public void addProperties(List<UDTValue> properties) {
    if (properties != null && !properties.isEmpty()) {
      for (UDTValue value : properties) {
        Property property = ResourceFactory.createProperty(value.getString("property"));
        RDFNode propertyValue;
        if (value.isNull("datatype")) {
          propertyValue = ResourceFactory.createResource(value.getString("value"));
        } else {
          propertyValue = ResourceFactory.createTypedLiteral(value.getString("value"),
              TypeMapper.getInstance().getSafeTypeByName(value.getString("datatype")));
        }

        addProperty(property, propertyValue);
      }
    }
  }

  public void addParameter(Resource parameter, RDFNode value) {
    parameters.put(parameter, value);
  }

  public String toInsertQuery() {
    if (properties.isEmpty()) {
      return String.format("INSERT INTO semiot.commandresult " +
          "(system_id, event_time, command_type) " +
          "VALUES ('%s', '%s', '%s')", systemId, eventTime, type);
    } else {
      StringBuilder builderProps = new StringBuilder("[");
      for (CommandProperty p : properties) {
        builderProps.append(p.toCQLValue()).append(",");
      }
      builderProps.deleteCharAt(builderProps.length() - 1);
      builderProps.append("]");

      if (parameters.isEmpty()) {
        return String.format("INSERT INTO semiot.commandresult " +
                "(system_id, event_time, command_properties, command_type)" +
                "VALUES ('%s', '%s', %s, '%s')",
            systemId, eventTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            builderProps.toString(), type);
      } else {
        StringBuilder builderParams = new StringBuilder("[");
        for (Resource parameter : parameters.keySet()) {
          builderParams.append("{").append("for_parameter:'").append(parameter.getURI())
              .append("',value:'").append(parameters.get(parameter).asLiteral().getLexicalForm())
              .append("'},");
        }
        builderParams.deleteCharAt(builderParams.length() - 1);
        builderParams.append("]");

        return String.format("INSERT INTO semiot.commandresult " +
                "(system_id, event_time, command_properties, command_parameters, command_type)" +
                "VALUES ('%s', '%s', %s, %s, '%s')",
            systemId, eventTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), builderProps.toString(),
            builderParams.toString(), type);
      }
    }
  }

  public Model toRDF() {
    Model model = ModelFactory.createDefaultModel();
    Resource commandResult = ResourceFactory.createResource();
    Resource command = ResourceFactory.createResource();
    Resource system = ResourceFactory.createResource(SENSOR_URI_PREFIX + systemId);

    model.add(commandResult, RDF.type, SEMIOT.CommandResult)
        .add(commandResult, SEMIOT.isResultOf, command)
        .add(commandResult, DUL.hasEventTime,
            eventTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        .add(commandResult, DUL.involvesAgent, system);

    model.add(command, RDF.type, SEMIOT.Command)
        .add(command, RDF.type, ResourceFactory.createResource(type))
        .add(command, DUL.involvesAgent, system);

    for (Resource parameter : parameters.keySet()) {
      Resource parameterResource = ResourceFactory.createResource();
      model.add(command, DUL.hasParameter, parameterResource)
          .add(parameterResource, SEMIOT.forParameter, parameter)
          .add(parameterResource, DUL.hasParameterDataValue, parameters.get(parameter));
    }

    for (CommandProperty property : properties) {
      model.add(command, property.property, property.value);
    }

    return model;
  }


  private class CommandProperty {

    private final Property property;
    private final RDFNode value;

    CommandProperty(Property property, RDFNode value) {
      this.property = property;
      this.value = value;
    }

    public String toCQLValue() {
      if (value.isLiteral()) {
        Literal l = (Literal) value;
        return String.format("{property:'%s',value:'%s',datatype:'%s'}",
            property.getURI(), l.getLexicalForm(), l.getDatatypeURI());
      } else {
        Resource r = (Resource) value;
        return String.format("{property:'%s',value:'%s',datatype:null}",
            property.getURI(), r.getURI());
      }
    }
  }
}
