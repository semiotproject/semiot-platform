package ru.semiot.platform.drivers.netatmo.temperature;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;

public class ScheduledPuller implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(
            ScheduledPuller.class);

    private final DeviceDriverImpl driver;
    private final NetAtmoAPI netAtmoAPI;

    public ScheduledPuller(DeviceDriverImpl driver) {
        this.driver = driver;

        this.netAtmoAPI = new NetAtmoAPI(
                driver.getConfiguration().get(Keys.CLIENT_APP_ID), 
                driver.getConfiguration().get(Keys.CLIENT_SECRET));
    }

    public void init() {
        if (netAtmoAPI.authenticate(
                driver.getConfiguration().get(Keys.USERNAME), 
                driver.getConfiguration().get(Keys.PASSWORD))) {
            logger.info("Successfully authenticated!");
        } else {
            logger.error("Couldn't authenticate!");

            throw new IllegalArgumentException();
        }
    }

    @Override
    public void run() {
        try {
            logger.info("Starting to pull...");

            JSONArray devices = netAtmoAPI.getPublicData(
                    driver.getConfiguration().get(Keys.LATITUDE_NORTH_EAST), 
                    driver.getConfiguration().get(Keys.LONGITUDE_NORTH_EAST),
                    driver.getConfiguration().get(Keys.LATITUDE_SOUTH_WEST),
                    driver.getConfiguration().get(Keys.LONGITUDE_SOUTH_WEST));

            if (devices != null) {
                logger.info("Loaded [{}] stations", devices.length());

                WeatherStationFactory wsFactory = new WeatherStationFactory(
                        Keys.DRIVER_PID);

                List<WeatherStation> stations = wsFactory.parse(devices);

                //Register and update already registered devices
                for (WeatherStation newDevice : stations) {
                    if (driver.isRegistered(newDevice.getId())) {
                        Device oldDevice = driver.getDeviceById(newDevice.getId());

                        if (!newDevice.equals(oldDevice)) {
                            logger.debug("New: {}", newDevice.getProperties());
                            logger.debug("Old: {}", oldDevice.getProperties());
                            
                            driver.updateDevice(oldDevice);

                            logger.debug("Device's [{}] data has changed. Updating.",
                                    newDevice.getId());
                        } else {
                            logger.debug("Device's [{}] data hasn't changed. Skipping.",
                                    newDevice.getId());
                        }
                    } else {
                        driver.registerDevice(newDevice);

                        logger.debug("New device [{}] found. Registering.",
                                newDevice.getId());
                    }
                }
            } else {
                logger.warn("Pulling failed!");
            }

            //TODO: Publish observations
        } catch (JSONException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

}
