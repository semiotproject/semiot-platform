package ru.semiot.platform.deviceproxyservice.api.drivers;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class Device {

  private final Map<String, String> properties = new HashMap<>();

  public Device(String id) {
    properties.put(DeviceProperties.DEVICE_ID, id);
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public String getProperty(String name) {
    return properties.get(name);
  }

  public void setProperty(String key, String value) {
    properties.put(key, value);
  }

  public String getId() {
    return properties.get(DeviceProperties.DEVICE_ID);
  }

  public abstract String getRDFTemplate();

  public String toTurtleString() {
    return TemplateUtils.resolve(getRDFTemplate(), properties);
  }
  
  public Model toDescriptionAsModel(Configuration configuration) {
    StringReader descr = new StringReader(TemplateUtils.resolve(toTurtleString(), configuration));

    Model model = ModelFactory.createDefaultModel().read(descr, null, RDFLanguages.strLangTurtle);

    return model;
  }

  public String toDescriptionAsString(Configuration configuration, Lang lang) {
    StringWriter writer = new StringWriter();

    toDescriptionAsModel(configuration).write(writer, lang.getName());

    return writer.toString();
  }

  public String toDescriptionAsString(Model model, Lang lang) {
    StringWriter writer = new StringWriter();

    model.write(writer, lang.getName());

    return writer.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (obj instanceof Device) {
      Device that = (Device) obj;

      return this.properties.equals(that.properties);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 89 * hash + Objects.hashCode(this.properties);
    return hash;
  }
}
