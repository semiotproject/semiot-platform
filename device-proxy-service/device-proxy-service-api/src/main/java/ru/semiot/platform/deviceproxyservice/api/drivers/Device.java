package ru.semiot.platform.deviceproxyservice.api.drivers;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class Device {

  private static final Logger logger = LoggerFactory.getLogger(Device.class);
  private final Map<String, Object> properties = new HashMap<>();

  public Device(String id) {
    this.properties.put(DeviceProperties.DEVICE_ID, id);
    this.properties.put(DeviceProperties.DEVICE_URI,
        "{{" + Keys.PLATFORM_SYSTEMS_URI_PREFIX + "}}/" + id);
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public String getProperty(String name) {
    return properties.get(name).toString();
  }

  public void setProperty(String key, String value) {
    properties.put(key, value);
  }

  public String getId() {
    return properties.get(DeviceProperties.DEVICE_ID).toString();
  }

  public abstract RDFTemplate getRDFTemplate();

  public Model toDescriptionAsModel(Configuration configuration) {
    Model model = ModelFactory.createDefaultModel().read(
        getRDFTemplate().resolveToReader(properties, configuration),
        null,
        getRDFTemplate().getRDFLanguage());

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
