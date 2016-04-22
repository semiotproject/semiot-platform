package ru.semiot.platform.deviceproxyservice.api.drivers;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public abstract class Command {

  private final Map<String, String> properties = new HashMap<>();

  public Command(String deviceId, ZonedDateTime dateTime) {
    properties.put(DeviceProperties.DEVICE_ID, deviceId);
    properties.put(DeviceProperties.ACTUATION_DATETIME,
        dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
  }

  public abstract String getRDFTemplate();

  public String toTurtleString() {
    return TemplateUtils.resolve(getRDFTemplate(), properties);
  }

  public Map<String, String> getProperties() {
    return properties;
  }
}
