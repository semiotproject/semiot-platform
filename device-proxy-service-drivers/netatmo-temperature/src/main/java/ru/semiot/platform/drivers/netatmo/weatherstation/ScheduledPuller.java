package ru.semiot.platform.drivers.netatmo.weatherstation;

import java.util.List;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceProperties;

public class ScheduledPuller implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(
            ScheduledPuller.class);

    private final DeviceDriverImpl driver;
    private final NetatmoAPI netAtmoAPI;

    public ScheduledPuller(DeviceDriverImpl driver) {
        this.driver = driver;

        this.netAtmoAPI = new NetatmoAPI(
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

            JSONArray data = netAtmoAPI.getPublicData(
                    driver.getConfiguration().get(Keys.LATITUDE_NORTH_EAST), 
                    driver.getConfiguration().get(Keys.LONGITUDE_NORTH_EAST),
                    driver.getConfiguration().get(Keys.LATITUDE_SOUTH_WEST),
                    driver.getConfiguration().get(Keys.LONGITUDE_SOUTH_WEST));

            if (data != null) {
                logger.info("Loaded [{}] stations", data.length());

                WeatherStationFactory wsFactory = new WeatherStationFactory(
                        Keys.DRIVER_PID);

                List<WeatherStation> stations = wsFactory.parseStations(data);
                
                logger.info("Found [{}] stations", stations.size());

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
                
                //Send new observations
                List<WeatherStationObservation> observations = wsFactory
                        .parseObservations(data);
                
                logger.info("Found [{}] observations", observations.size());
                
                for(WeatherStationObservation newObs : observations) {
                    String deviceId = newObs.getProperty(
                            DeviceProperties.DEVICE_ID);
                    String type = newObs.getProperty(
                            NetatmoDeviceProperties.OBSERVATION_TYPE);
                    
                    WeatherStationObservation oldObs = driver.getObservation(
                            deviceId, type);
                    
                    if(oldObs == null || !newObs.getProperty(DeviceProperties.OBSERVATION_TIMESTAMP)
                            .equalsIgnoreCase(oldObs.getProperty(DeviceProperties.OBSERVATION_TIMESTAMP))) {
                        driver.publishNewObservation(newObs);
                    } else {
                        logger.debug("Observation [device={}, type={}] isn't new. Skipping", 
                                deviceId, type);
                    }
                }
            } else {
                logger.warn("Pulling failed!");
            }
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

}
