package ru.semiot.platform.drivers.netatmo.temperature;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.Observation;

public class TemperatureObservation extends Observation {
    
    private static final String TEMPLATE_PATH
            = "/ru/semiot/platform/drivers/netatmo/temperature/observation.ttl";
    private static String DESCRIPTION_TEMPLATE;
    
    static {
        try {
            DESCRIPTION_TEMPLATE = IOUtils.toString(
                    TemperatureObservation.class.getResourceAsStream(TEMPLATE_PATH));
        } catch (IOException ex) {
            LoggerFactory.getLogger(WeatherStation.class)
                    .error(ex.getMessage(), ex);
        }
    }
    
    public TemperatureObservation(String deviceId, String timestamp, String value) {
        super(deviceId, timestamp);
        
        getProperties().put(NetAtmoDeviceProperties.OBSERVATION_VALUE, value);
    }

    @Override
    public String getRDFTemplate() {
        return DESCRIPTION_TEMPLATE;
    }
    
}
