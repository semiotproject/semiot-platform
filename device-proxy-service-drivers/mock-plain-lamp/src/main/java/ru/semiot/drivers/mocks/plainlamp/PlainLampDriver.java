package ru.semiot.drivers.mocks.plainlamp;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.Configuration;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;
import ru.semiot.platform.deviceproxyservice.api.drivers.DriverInformation;

import java.net.URI;
import java.util.Dictionary;

public class PlainLampDriver implements DeviceDriver, ManagedService {

    private static final Logger logger = LoggerFactory.getLogger(
            PlainLampDriver.class);
    private static final String DRIVER_NAME = "Plain Lamp (Mock) Driver";
    private final Configuration configuration = new Configuration();
    private final DriverInformation info = new DriverInformation(
            Activator.DRIVER_PID,
            URI.create("https://raw.githubusercontent.com/semiotproject/semiot-platform/" +
                    "master/device-proxy-service-drivers/mock-plain-lamp/" +
                    "src/main/resources/ru/semiot/drivers/mocks/plainlamp/prototype.ttl#"));

    private volatile DeviceManager deviceManager;

    public void start() {
        deviceManager.registerDriver(info);

        logger.info("{} started!", DRIVER_NAME);
    }

    public void stop() {
        logger.info("{} stopped!", DRIVER_NAME);
    }

    public void updated(Dictionary dictionary) throws ConfigurationException {
        synchronized (this) {
            if(dictionary != null) {
                if(configuration.isConfigured()) {
                    logger.debug("Configured!");
                } else {
                    logger.warn("Is already configured! Skipping.");
                }
            } else {
                logger.debug("Configuration is empty. Skipping.");
            }
        }
    }

    public String getDriverName() {
        return DRIVER_NAME;
    }
}
