package ru.semiot.platform.deviceproxyservice.api.drivers;

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
