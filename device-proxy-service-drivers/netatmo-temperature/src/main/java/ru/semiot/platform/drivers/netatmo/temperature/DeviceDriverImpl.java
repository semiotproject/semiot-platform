package ru.semiot.platform.drivers.netatmo.temperature;

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

public class DeviceDriverImpl implements DeviceDriver, ManagedService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceDriverImpl.class);

    private final Map<String, Device> devicesMap
            = Collections.synchronizedMap(new HashMap<>());
    private final String driverName = "Netatmo temperature";
    private final Configuration configuration = new Configuration();

    private volatile DeviceManager deviceManager;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture handle = null;

    public void start() {
        logger.info("{} started!", driverName);

        this.scheduler = Executors.newScheduledThreadPool(1);
        startSheduled();
    }

    public void stop() {
        stopSheduled();

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

    public void registerDevice(Device device) {
        devicesMap.put(device.getId(), device);

        deviceManager.register(device);
    }

    public boolean isRegistered(String id) {
        return devicesMap.containsKey(id);
    }

    public Device getDeviceById(String id) {
        return devicesMap.get(id);
    }

    public void updateDevice(Device newDevice) {
        devicesMap.put(newDevice.getId(), newDevice);

        //TODO: Trigger update in the database
    }

    public int getNumberOfRegisteredDevices() {
        return devicesMap.size();
    }

    public Set<String> getIDsOfRegisteredDevices() {
        return devicesMap.keySet();
    }

    @Override
    public String getDriverName() {
        return driverName;
    }

    public void startSheduled() {
        if (this.handle != null) {
            logger.debug("Try to stop scheduler");
            stopSheduled();
        }

        ScheduledPuller puller = new ScheduledPuller(this);
        puller.init();

        this.handle = this.scheduler.scheduleAtFixedRate(
                puller,
                configuration.getAsLong(Keys.POLLING_START_PAUSE),
                configuration.getAsLong(Keys.POLLING_INTERVAL),
                TimeUnit.MINUTES);

        logger.debug("Polling scheduled. Interval {} minutes",
                configuration.get(Keys.POLLING_INTERVAL));
    }

    public void stopSheduled() {
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

//    private void sendMessage(String value, long timestamp, String hash) {
//        if (value != null) {
//            String topic = templateTopic.replace("${DEVICE_HASH}", hash);
//
//            final String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
//                    .format(new Date(timestamp));
//
//            String message = driver
//                    .getTemplateObservation()
//                    .replace("${DOMAIN}", driver.getDomain())
//                    .replace("${SYSTEM_PATH}", driver.getPathSystemUri())
//                    .replace("${SENSOR_PATH}", driver.getPathSensorUri())
//                    .replace("${DEVICE_HASH}", hash)
//                    .replace("${SENSOR_ID}", "1")
//                    .replace("${TIMESTAMP}", String.valueOf(timestamp))
//                    .replace("${DATETIME}", date)
//                    .replace("${VALUE}", value);
//
//            driver.publish(topic, message);
//        } else {
//            logger.warn(hash + " has unknown value (null)");
//        }
//    }
}
