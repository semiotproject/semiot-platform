package ru.semiot.platform.deviceproxyservice.api.drivers;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class Command {

  private final Map<String, Object> properties = new HashMap<>();

  public Command(String processId, String commandId) {
    this.properties.put(DeviceProperties.PROCESS_ID, processId);
    this.properties.put(DeviceProperties.PROCESS_URI,
        "{{" + Keys.PLATFORM_PROCESS_URI_PREFIX + "}}/" + processId);
    this.properties.put(DeviceProperties.COMMAND_ID, commandId);
  }

  public String get(String name) {
    return properties.get(name).toString();
  }

  public int getAsInteger(String name) {
    return Integer.valueOf(properties.get(name).toString());
  }

  public Map<String, Object> getAll() {
    return properties;
  }

  public void add(String name, Object value) {
    properties.put(name, value);
  }

  public void add(String name, String value) {
    properties.put(name, value);
  }

  public void add(String name, int value) {
    properties.put(name, Integer.toString(value));
  }

}
