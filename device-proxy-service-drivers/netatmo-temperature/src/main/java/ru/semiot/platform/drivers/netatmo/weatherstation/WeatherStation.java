package ru.semiot.platform.drivers.netatmo.weatherstation;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;

public class WeatherStation extends Device {

    private static final String TEMPLATE_PATH
            = "/ru/semiot/platform/drivers/netatmo/weatherstation/description.ttl";
    private static String DESCRIPTION_TEMPLATE;

    static {
        try {
            DESCRIPTION_TEMPLATE = IOUtils.toString(
                    WeatherStationFactory.class.getResourceAsStream(TEMPLATE_PATH));
        } catch (IOException ex) {
            LoggerFactory.getLogger(WeatherStation.class)
                    .error(ex.getMessage(), ex);
        }
    }

    public WeatherStation(String id, String latitude, String longitude, 
            String altitude) {
        super(id);
        setProperty(NetatmoDeviceProperties.DEVICE_ID, id);
        setProperty(NetatmoDeviceProperties.LATITUDE, latitude);
        setProperty(NetatmoDeviceProperties.LONGITUDE, longitude);
        setProperty(NetatmoDeviceProperties.ALTITUDE, altitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof WeatherStation) {
            WeatherStation that = (WeatherStation) obj;

            return super.equals(obj);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash += super.hashCode();
        return hash;
    }

    @Override
    public String getRDFTemplate() {
        return DESCRIPTION_TEMPLATE;
    }

}
