package ru.semiot.platform.deviceproxyservice.api.drivers;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import ru.semiot.commons.namespaces.DUL;
import ru.semiot.commons.namespaces.NamespaceUtils;
import ru.semiot.commons.namespaces.SEMIOT;
import ru.semiot.commons.rdf.ModelUtils;
import ru.semiot.commons.rdf.ResourceUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Command {

  private static final String URI_PREFIX = "http://w3id.org/semiot/ontologies/semiot#";

  private final String deviceId;
  private final Resource type;
  private final Map<String, RDFNode> properties = new HashMap<>();
  private final Map<String, CommandParameter> parameters = new HashMap<>();

  public static final String TYPE_SWITCHPROCESSCOMMAND = URI_PREFIX + "SwitchProcessCommand";
  public static final String PROP_TARGETPROCESS = URI_PREFIX + "targetProcess";

  public Command(String deviceId, String type) {
    this.deviceId = deviceId;
    this.type = ResourceFactory.createResource(type);
  }

  public Command(Model command) {
    Resource commandResource = command.listSubjectsWithProperty(RDF.type, SEMIOT.Command).next();
    List<RDFNode> types = ModelUtils.objectsOfProperty(command, RDF.type, SEMIOT.Command);
    if (!types.isEmpty()) {
      this.type = types.get(0).asResource();

      Resource device = ModelUtils.objectOfProperty(command, DUL.involvesAgent).asResource();
      this.deviceId = NamespaceUtils.extractLocalName(device.getURI());

      StmtIterator propertiesIterator = command.listStatements(
          commandResource, null, (RDFNode) null);
      while (propertiesIterator.hasNext()) {
        Statement stmt = propertiesIterator.next();
        Property property = stmt.getPredicate();
        if (!property.equals(DUL.hasParameter)) {
          properties.put(property.getURI(), stmt.getObject());
        } else {
          Resource parameter = (Resource) command
              .listObjectsOfProperty((Resource) stmt.getObject(), SEMIOT.forParameter).next();
          RDFNode value = command
              .listObjectsOfProperty((Resource) stmt.getObject(), DUL.hasParameterDataValue).next();
          parameters.put(parameter.getURI(), new CommandParameter(parameter, value));
        }
      }
    } else {
      throw new IllegalArgumentException();
    }
  }

  public String getDeviceId() {
    return deviceId;
  }

  public String getCommandType() {
    return this.type.getURI();
  }

  public void addProperty(String uri, String value) {
    properties.put(uri, ResourceUtils.toRDFNode(value));
  }

  public void addParameter(String uri, String value) {
    parameters.put(uri, new CommandParameter(uri, value));
  }

  public void addParameter(String uri, int value) {
    parameters.put(uri, new CommandParameter(
        uri,
        ResourceFactory.createTypedLiteral(Integer.toString(value), XSDDatatype.XSDinteger)));
  }

  public String getPropertyValue(String uri) {
    RDFNode value = properties.get(uri);
    if (value.isLiteral()) {
      return value.asLiteral().getString();
    } else {
      return value.asResource().getURI();
    }
  }

  public Model toRDFAsModel(Map<String, String> configuration) {
    Model model = ModelFactory.createDefaultModel();
    Resource commandResource = ResourceFactory.createResource();

    model.add(commandResource, RDF.type, SEMIOT.Command)
        .add(commandResource, RDF.type, type);

    for (String uri : properties.keySet()) {
      model.add(commandResource, ResourceFactory.createProperty(uri), properties.get(uri));
    }

    for (String parameter : parameters.keySet()) {
      Resource node = ResourceFactory.createResource();
      model.add(commandResource, DUL.hasParameter, node)
          .add(node, SEMIOT.forParameter, parameters.get(parameter).getParameter())
          .add(node, DUL.hasParameterDataValue, parameters.get(parameter).getValue());
    }

    return model;
  }

  private class CommandParameter {
    private final Resource parameter;
    private final Literal value;

    public CommandParameter(String parameter, String value) {
      this(ResourceFactory.createResource(parameter), ResourceUtils.toRDFNode(value).asLiteral());
    }

    public CommandParameter(String parameter, RDFNode value) {
      this(ResourceFactory.createResource(parameter), value);
    }

    public CommandParameter(Resource parameter, RDFNode value) {
      this.parameter = parameter;
      this.value = value.asLiteral();
    }

    public Resource getParameter() {
      return parameter;
    }

    public RDFNode getValue() {
      return value;
    }
  }
}
