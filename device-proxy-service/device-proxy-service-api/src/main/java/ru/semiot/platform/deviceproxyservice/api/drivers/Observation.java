package ru.semiot.platform.deviceproxyservice.api.drivers;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public abstract class Observation {

  private final Map<String, Object> properties = new HashMap<>();

  /**
   * @param timestamp number of seconds or milliseconds since the Unix Epoch
   */
  public Observation(String deviceId, String sensorId, String timestamp) {
    properties.put(DeviceProperties.DEVICE_ID, deviceId);
    properties.put(DeviceProperties.SENSOR_ID, sensorId);
    properties.put(DeviceProperties.OBSERVATION_TIMESTAMP, timestamp);

    //Timestamp to ISO 8601 date and time
    String dateTime = null;
    if (timestamp.length() > 10) {
      dateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME
          .withZone(ZoneOffset.UTC)
          .format(Instant.ofEpochMilli(Long.valueOf(timestamp)));
    } else {
      dateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME
          .withZone(ZoneOffset.UTC)
          .format(Instant.ofEpochSecond(Long.valueOf(timestamp)));
    }
    properties.put(DeviceProperties.OBSERVATION_DATETIME, dateTime);
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public String getProperty(String name) {
    return properties.get(name).toString();
  }

  public abstract RDFTemplate getRDFTemplate();

  public Model toObservationAsModel(Map<String, Object> properties, Configuration configuration) {
    StringReader descr = getRDFTemplate().resolveToReader(
        this.properties, properties, configuration);

    Model model = ModelFactory.createDefaultModel().read(descr, null, RDFLanguages.strLangTurtle);

    return model;
  }

  public String toObservationAsString(Map<String, Object> properties, Configuration configuration,
      Lang lang) {
    StringWriter writer = new StringWriter();

    toObservationAsModel(properties, configuration).write(writer, lang.getName());

    return writer.toString();
  }

  public boolean equalsIgnoreTimestamp(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof Observation)) {
      return false;
    }
    Observation m = (Observation) o;
    if (m.properties.size() != this.properties.size()) {
      return false;
    }

    try {
      Iterator<Map.Entry<String, Object>> i = this.properties.entrySet().iterator();
      while (i.hasNext()) {
        Map.Entry<String, Object> e = i.next();
        String key = e.getKey();
        Object value = e.getValue();
        if (value == null) {
          if (!(m.properties.get(key) == null && m.properties.containsKey(key))) {
            return false;
          }
        } else {
          if (!value.equals(m.properties.get(key))) {
            if (!key.equals(DeviceProperties.OBSERVATION_TIMESTAMP)
                && !key.equals(DeviceProperties.OBSERVATION_DATETIME)) {
              return false;
            }
          }
        }
      }
    } catch (ClassCastException | NullPointerException unused) {
      return false;
    }

    return true;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (obj instanceof Observation) {
      Observation that = (Observation) obj;

      return this.properties.equals(that.properties);
    }

    return false;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 59 * hash + Objects.hashCode(this.properties);
    return hash;
  }

}
