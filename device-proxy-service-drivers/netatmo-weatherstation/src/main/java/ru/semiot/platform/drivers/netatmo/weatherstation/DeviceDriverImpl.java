package ru.semiot.platform.drivers.netatmo.weatherstation;

import java.net.URI;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
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

    private volatile DeviceManager deviceManager;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture handle = null;

    public void start() {
        logger.info("{} started!", driverName);

        DriverInformation info = new DriverInformation(
                Keys.DRIVER_PID,
                URI.create("https://raw.githubusercontent.com/semiotproject/semiot-platform/thesis-experiments/device-proxy-service-drivers/netatmo-temperature/src/main/resources/ru/semiot/platform/drivers/netatmo/weatherstation/prototype.ttl#NetatmoWeatherStationOutdoorModule"));

        deviceManager.registerDriver(info);

        this.scheduler = Executors.newScheduledThreadPool(1);
        startScheduled();
    }

    public void stop() {
        stopScheduled();

        logger.info("{} stopped!", driverName);
    }

    @Override
    public void updated(Dictionary dictionary) throws ConfigurationException {
        synchronized (this) {
            if (dictionary != null) {
                if (!configuration.isConfigured()) {
                    configuration.putAll(dictionary);

                    configuration.setConfigured();
                } else {
                    logger.warn("Driver is already configured! Ignoring it");
                }
            } else {
                logger.debug("Configuration is empty. Skipping it");
            }
        }
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

        deviceManager.registerDevice(device);
    }

    public void publishNewObservation(WeatherStationObservation observation) {
        //Replace previous observation
        String deviceId = observation.getProperty(NetatmoDeviceProperties.DEVICE_ID);
        String type = observation.getProperty(NetatmoDeviceProperties.OBSERVATION_TYPE);
        observationsMap.put(toObsKey(deviceId, type), observation);

        deviceManager.registerObservation(devicesMap.get(deviceId), observation);
    }

    public void startScheduled() {
        if (this.handle != null) {
            logger.debug("Try to stop scheduler");
            stopScheduled();
        }

        ScheduledPuller puller = new ScheduledPuller(this);
        puller.init();

        this.handle = this.scheduler.scheduleAtFixedRate(
                puller,
                configuration.getAsLong(Keys.POLLING_START_PAUSE),
                configuration.getAsLong(Keys.POLLING_INTERVAL),
                TimeUnit.MINUTES);

        logger.debug("Polling scheduled. Starts in {}min with interval {}min",
                configuration.get(Keys.POLLING_START_PAUSE),
                configuration.get(Keys.POLLING_INTERVAL));
    }

    public void stopScheduled() {
        logger.debug("Hello from stopScheduled!");
        if (handle == null) {
            return;
        }

        handle.cancel(true);
        handle = null;

        scheduler.shutdown();
        try {
            scheduler.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            logger.warn(ex.getMessage(), ex);
        }
        scheduler.shutdownNow();
        logger.debug("UScheduled stoped");
    }
    
    private String toObsKey(String deviceId, String type) {
        return deviceId + "-" + type;
    }
}
