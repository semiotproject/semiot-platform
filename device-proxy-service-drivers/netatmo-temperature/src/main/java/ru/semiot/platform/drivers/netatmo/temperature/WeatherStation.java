package ru.semiot.platform.drivers.netatmo.temperature;

import java.io.IOException;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;

public class WeatherStation extends Device {

    private static final String TEMPLATE_PATH
            = "/ru/semiot/platform/drivers/netatmo/temperature/description.ttl";
    private static String DESCRIPTION_TEMPLATE;
    
    private Observation temperature;

    static {
        try {
            DESCRIPTION_TEMPLATE = IOUtils.toString(
                    WeatherStationFactory.class.getResourceAsStream(TEMPLATE_PATH));
        } catch (IOException ex) {
            LoggerFactory.getLogger(WeatherStation.class)
                    .error(ex.getMessage(), ex);
        }
    }

    public WeatherStation(String id, double latitude, double longitude) {
        super(id);
        setProperty(NetAtmoDeviceProperties.DEVICE_ID, id);
        setProperty(NetAtmoDeviceProperties.LATITUDE, String.valueOf(latitude));
        setProperty(NetAtmoDeviceProperties.LONGITUDE, String.valueOf(longitude));
    }

    public Observation getTemperature() {
        return temperature;
    }

    public void setTemperature(Observation observation) {
        this.temperature = observation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof WeatherStation) {
            WeatherStation that = (WeatherStation) obj;

            return this.temperature.equals(that.temperature)
                    && super.equals(obj);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.temperature);
        hash += super.hashCode();
        return hash;
    }

    @Override
    public String getRDFTemplate() {
        return DESCRIPTION_TEMPLATE;
    }

    public static class Observation {

        private final String timestamp;
        private final double value;

        public Observation(String timestamp, double value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public double getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof Observation) {
                Observation that = (Observation) obj;

                return this.timestamp.equals(that.timestamp)
                        && this.value == that.value;
            }

            return false;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 11 * hash + Objects.hashCode(this.timestamp);
            hash = 11 * hash + Objects.hashCode(this.value);
            return hash;
        }

    }

}
