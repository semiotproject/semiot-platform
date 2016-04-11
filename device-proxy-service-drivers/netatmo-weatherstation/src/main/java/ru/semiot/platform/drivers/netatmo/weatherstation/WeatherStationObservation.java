package ru.semiot.platform.drivers.netatmo.weatherstation;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.Observation;

public class WeatherStationObservation extends Observation {

    private static final String TEMPERATURE_TEMPLATE_PATH
            = "/ru/semiot/platform/drivers/netatmo/weatherstation/temperature-observation.ttl";
    private static final String HUMIDITY_TEMPLATE_PATH
            = "/ru/semiot/platform/drivers/netatmo/weatherstation/humidity-observation.ttl";
    private static String TEMPERATURE_TEMPLATE;
    private static String HUMIDITY_TEMPLATE;
    
    public static final String TEMPERATURE_TYPE = "temperature";
    public static final String HUMIDITY_TYPE = "humidity";

    static {
        try {
            TEMPERATURE_TEMPLATE = IOUtils.toString(
                    WeatherStationObservation.class.getResourceAsStream(TEMPERATURE_TEMPLATE_PATH));
            HUMIDITY_TEMPLATE = IOUtils.toString(
                    WeatherStationObservation.class.getResourceAsStream(HUMIDITY_TEMPLATE_PATH));
        } catch (IOException ex) {
            LoggerFactory.getLogger(WeatherStation.class)
                    .error(ex.getMessage(), ex);
        }
    }

    public WeatherStationObservation(String deviceId, String timestamp,
            String value, String type) {
        super(deviceId, timestamp);

        getProperties().put(NetatmoDeviceProperties.OBSERVATION_VALUE, value);
        getProperties().put(NetatmoDeviceProperties.OBSERVATION_TYPE, type);
    }

    @Override
    public String getRDFTemplate() {
        if (getProperties().get(NetatmoDeviceProperties.OBSERVATION_TYPE)
                .equalsIgnoreCase(TEMPERATURE_TYPE)) {
            return TEMPERATURE_TEMPLATE;
        }
        if (getProperties().get(NetatmoDeviceProperties.OBSERVATION_TYPE)
                .equalsIgnoreCase(HUMIDITY_TYPE)) {
            return HUMIDITY_TEMPLATE;
        }

        throw new IllegalStateException();
    }

}
