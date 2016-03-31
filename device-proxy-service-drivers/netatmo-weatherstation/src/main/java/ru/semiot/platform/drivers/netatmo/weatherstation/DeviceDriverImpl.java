package ru.semiot.platform.drivers.netatmo.weatherstation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.Configuration;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;
import ru.semiot.platform.deviceproxyservice.api.drivers.DriverInformation;

public class DeviceDriverImpl implements DeviceDriver, ManagedService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceDriverImpl.class);

    private final Map<String, Device> devicesMap
            = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, WeatherStationObservation> observationsMap
            = Collections.synchronizedMap(new HashMap<>());
    private final String driverName = "Netatmo.com (only temperature)";
    private final Configuration configuration = new Configuration();
    private final DriverInformation info = new DriverInformation(
            Keys.DRIVER_PID,
            URI.create("https://raw.githubusercontent.com/semiotproject/semiot-platform/master/device-proxy-service-drivers/netatmo-weatherstation/src/main/resources/ru/semiot/platform/drivers/netatmo/weatherstation/prototype.ttl#NetatmoWeatherStationOutdoorModule"));
    ; 

    private volatile DeviceManager deviceManager;

    private ScheduledExecutorService scheduler;
    private List<ScheduledFuture> handles = null;
    private List<Configuration> configurations;
    private NetatmoAPI netAtmoAPI;
    private List<Integer> countsRepeatableProperties;

    public void start() {
        logger.info("{} started!", driverName);
        deviceManager.registerDriver(info);

        handles = new ArrayList<>();
        this.scheduler = Executors.newScheduledThreadPool(countsRepeatableProperties.size());
        logger.debug("Try to start {} pullers", countsRepeatableProperties.size());
        for (Configuration cfg : configurations) {
            handles.add(startPuller(cfg));
        }
        logger.debug("All pullers started");
    }

    public void stop() {
        logger.debug("Try to stop {} pullers", handles.size());
        for (ScheduledFuture handle : handles) {
            stopPuller(handle);
        }
        logger.debug("All pullers stoped");
        handles = null;
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(1, TimeUnit.MINUTES);
        }
        catch (InterruptedException ex) {
            logger.warn(ex.getMessage(), ex);
        }
        scheduler.shutdownNow();
        logger.debug("Sheduler stoped");
        logger.info("{} stopped!", driverName);
    }

    @Override
    public void updated(Dictionary dictionary) throws ConfigurationException {
        synchronized (this) {
            if (dictionary != null) {
                if (!configuration.isConfigured()) {
                    logger.debug("Configuration got");
                    try {
                        configuration.putAll(dictionary);
                        Configuration commonConfiguration = getCommonConfiguration();
                        netAtmoAPI = new NetatmoAPI(commonConfiguration.get(Keys.CLIENT_APP_ID),
                                                    commonConfiguration.get(Keys.CLIENT_SECRET));
                        checkConnection(commonConfiguration.get(Keys.USERNAME),
                                        commonConfiguration.get(Keys.PASSWORD));
                        countsRepeatableProperties = getCountsRepeatableProperties(Keys.AREA);
                        configurations = getConfigurations(countsRepeatableProperties);
                        configuration.setConfigured();
                        logger.info("Received configuration is correct!");
                    }
                    catch (ConfigurationException ex) {
                        configuration.clear();
                        throw ex;
                    }
                } else {
                    logger.warn("Driver is already configured! Ignoring it");
                }
            } else {
                logger.debug("Configuration is empty. Skipping it");
            }
        }
    }

    public void checkConnection(String user, String pass) throws ConfigurationException {
        logger.debug("Try to authenticate with server");
        if (netAtmoAPI.authenticate(user, pass)) {
            logger.info("Successfully authenticated!");
        } else {
            logger.error("Couldn't authenticate!");
            throw new ConfigurationException(user + ":" + pass, "Login or password is incorrect");
        }
    }

    private List<Configuration> getConfigurations(List<Integer> counts) throws ConfigurationException {
        logger.debug("Try to get repeatable configuration for each puller");
        List<Configuration> conf = new ArrayList<>();
        for (int i : counts) {
            Configuration cfg = getAreaConfiguration(i);
            cfg.put(Keys.ONLY_NEW_OBS, configuration.get(Keys.ONLY_NEW_OBS));
            conf.add(cfg);
        }
        return conf;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public boolean isRegistered(String id) {
        return devicesMap.containsKey(id);
    }

    public Device getDeviceById(String id) {
        return devicesMap.get(id);
    }

    public int getNumberOfRegisteredDevices() {
        return devicesMap.size();
    }

    public Set<String> getIDsOfRegisteredDevices() {
        return devicesMap.keySet();
    }

    public WeatherStationObservation getObservation(String deviceId, String type) {
        return observationsMap.get(toObsKey(deviceId, type));
    }

    @Override
    public String getDriverName() {
        return driverName;
    }

    public void updateDevice(Device newDevice) {
        devicesMap.put(newDevice.getId(), newDevice);

        //TODO: Trigger update in the database
    }

    public void registerDevice(Device device) {
        devicesMap.put(device.getId(), device);

        deviceManager.registerDevice(info, device);
    }

    public void publishNewObservation(WeatherStationObservation observation) {
        //Replace previous observation
        String deviceId = observation.getProperty(NetatmoDeviceProperties.DEVICE_ID);
        String type = observation.getProperty(NetatmoDeviceProperties.OBSERVATION_TYPE);
        observationsMap.put(toObsKey(deviceId, type), observation);

        deviceManager.registerObservation(devicesMap.get(deviceId), observation);
    }

    public ScheduledFuture startPuller(Configuration config) {
        logger.debug("Try to start puller!");
        logger.debug("Config is " + config.toString());
        ScheduledPuller puller = new ScheduledPuller(this, config, netAtmoAPI);

        logger.debug("Try to schedule polling. Starts in {}min with interval {}min with configuration [{}]",
                     configuration.get(Keys.POLLING_START_PAUSE),
                     configuration.get(Keys.POLLING_INTERVAL),
                     config.toString());

        ScheduledFuture handle = this.scheduler.scheduleAtFixedRate(
                puller,
                configuration.getAsLong(Keys.POLLING_START_PAUSE),
                configuration.getAsLong(Keys.POLLING_INTERVAL),
                TimeUnit.MINUTES);

        logger.debug("Puller started!");
        return handle;
    }

    public void stopPuller(ScheduledFuture handle) {
        logger.debug("Try to stop puller!");
        if (handle == null) {
            return;
        }
        handle.cancel(true);
        logger.debug("Puller stoped!");
    }

    private String toObsKey(String deviceId, String type) {
        return deviceId + "-" + type;
    }

    private List<Integer> getCountsRepeatableProperties(String propPrefix) throws ConfigurationException {
        logger.debug("Try to get count of repeatable property \"{}\"", propPrefix);
        List<Integer> counts = new ArrayList<>();
        int index;

        for (String key : configuration.keySet()) {
            if (key.contains(propPrefix) && !counts.contains(
                    index = Integer.parseInt(key.substring(0, key.indexOf("." + propPrefix))))) {
                counts.add(index);
            }
        }
        if (counts.isEmpty()) {
            logger.error("Bad repeatable configuration! Did not find a repeatable property");
            throw new ConfigurationException(propPrefix, "Did not find a repeatable property");
        }
        return counts;
    }

    private Configuration getCommonConfiguration() throws ConfigurationException {
        logger.debug("Try to get common configuration");
        Configuration config = new Configuration();
        try {
            //Put only needed properties
            config.put(Keys.CLIENT_APP_ID, configuration.get(Keys.CLIENT_APP_ID));
            config.put(Keys.CLIENT_SECRET, configuration.get(Keys.CLIENT_SECRET));
            config.put(Keys.USERNAME, configuration.get(Keys.USERNAME));
            config.put(Keys.PASSWORD, configuration.get(Keys.PASSWORD));
            config.put(Keys.POLLING_START_PAUSE, configuration.get(Keys.POLLING_START_PAUSE));
            config.put(Keys.POLLING_INTERVAL, configuration.get(Keys.POLLING_INTERVAL));
            if (configuration.get(Keys.ONLY_NEW_OBS).equalsIgnoreCase("true")
                    || configuration.get(Keys.ONLY_NEW_OBS).equalsIgnoreCase("false")) {
                config.put(Keys.ONLY_NEW_OBS, configuration.get(Keys.ONLY_NEW_OBS));
            }
            else{
                throw new java.lang.NullPointerException();
            }
        }
        catch (java.lang.NullPointerException ex) {
            logger.error("Bad common configuration! Can not extract fields");
            throw new ConfigurationException("Common property", "Can not extract fields", ex);
        }
        return config;
    }

    private Configuration getAreaConfiguration(int area) throws ConfigurationException {
        logger.debug("Try to get configuration for {} area", area);
        Configuration config = new Configuration();
        double lon_ne, lat_ne, lon_sw, lat_sw;
        try {

            lon_ne = Double.parseDouble(configuration.get(area + "." + Keys.AREA + ".1.longitude"));
            lat_ne = Double.parseDouble(configuration.get(area + "." + Keys.AREA + ".1.latitude"));
            lon_sw = Double.parseDouble(configuration.get(area + "." + Keys.AREA + ".2.longitude"));
            lat_sw = Double.parseDouble(configuration.get(area + "." + Keys.AREA + ".2.latitude"));
            if (lon_ne > 180 || lon_ne < -180 || lon_sw > 180 || lon_sw < -180
                    || lat_ne > 90 || lat_ne < -90 || lat_sw > 90 || lat_sw < -90
                    || lon_ne < lon_sw && lat_ne > lat_sw || lon_ne > lon_sw && lat_ne < lat_sw) {
                throw new java.lang.NullPointerException();
            }
            if (lon_ne < lon_sw && lat_ne < lat_sw) {
                logger.debug("Swop values");
                config.put(Keys.LONGITUDE_NORTH_EAST, String.valueOf(lon_sw));
                config.put(Keys.LATITUDE_NORTH_EAST, String.valueOf(lat_sw));
                config.put(Keys.LONGITUDE_SOUTH_WEST, String.valueOf(lon_ne));
                config.put(Keys.LATITUDE_SOUTH_WEST, String.valueOf(lat_ne));
            } else {
                config.put(Keys.LONGITUDE_NORTH_EAST, String.valueOf(lon_ne));
                config.put(Keys.LATITUDE_NORTH_EAST, String.valueOf(lat_ne));
                config.put(Keys.LONGITUDE_SOUTH_WEST, String.valueOf(lon_sw));
                config.put(Keys.LATITUDE_SOUTH_WEST, String.valueOf(lat_sw));
            }
        }
        catch (java.lang.NullPointerException ex) {
            logger.error("Bad repeatable configuration! Can not extract field of property {}.{}", Keys.AREA, area);
            throw new ConfigurationException(Keys.AREA + "." + area, "Can not extract field of repeatable property", ex);
        }
        return config;
    }
}
