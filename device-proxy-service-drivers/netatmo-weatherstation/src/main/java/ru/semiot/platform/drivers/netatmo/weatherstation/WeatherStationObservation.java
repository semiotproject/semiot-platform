package ru.semiot.platform.drivers.netatmo.weatherstation;

import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.Observation;
import ru.semiot.platform.deviceproxyservice.api.drivers.RDFTemplate;

import java.io.IOException;

public class WeatherStationObservation extends Observation {

  private static final String TEMPERATURE_TEMPLATE_PATH =
      "/ru/semiot/platform/drivers/netatmo/weatherstation/temperature-observation.ttl";
  private static final String HUMIDITY_TEMPLATE_PATH =
      "/ru/semiot/platform/drivers/netatmo/weatherstation/humidity-observation.ttl";
  private static RDFTemplate TEMPERATURE_TEMPLATE;
  private static RDFTemplate HUMIDITY_TEMPLATE;

  public static final String TEMPERATURE_TYPE = "temperature";
  public static final String HUMIDITY_TYPE = "humidity";
  public static final String TEMPERATURE_TEMPLATE_SENSOR = "${SYSTEM_ID}-temperature";
  public static final String HUMIDITY_TEMPLATE_SENSOR = "${SYSTEM_ID}-humidity";

  static {
    try {
      TEMPERATURE_TEMPLATE = new RDFTemplate("temperature",
          WeatherStationObservation.class.getResourceAsStream(TEMPERATURE_TEMPLATE_PATH));
      HUMIDITY_TEMPLATE = new RDFTemplate("humidity",
          WeatherStationObservation.class.getResourceAsStream(HUMIDITY_TEMPLATE_PATH));
    } catch (IOException ex) {
      LoggerFactory.getLogger(WeatherStation.class).error(ex.getMessage(), ex);
    }
  }

  public WeatherStationObservation(String deviceId, String sensorId, String timestamp, String value,
      String type) {
    super(deviceId, sensorId, timestamp);

    getProperties().put(NetatmoDeviceProperties.OBSERVATION_VALUE, value);
    getProperties().put(NetatmoDeviceProperties.OBSERVATION_TYPE, type);
  }

  @Override
  public RDFTemplate getRDFTemplate() {
    if (getProperty(NetatmoDeviceProperties.OBSERVATION_TYPE).equalsIgnoreCase(TEMPERATURE_TYPE)) {
      return TEMPERATURE_TEMPLATE;
    }
    if (getProperty(NetatmoDeviceProperties.OBSERVATION_TYPE).equalsIgnoreCase(HUMIDITY_TYPE)) {
      return HUMIDITY_TEMPLATE;
    }

    throw new IllegalStateException();
  }

}
