package ru.semiot.drivers.mocks.plainlamp;

import org.apache.jena.rdf.model.Model;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.*;

import java.net.URI;
import java.util.Dictionary;

public class PlainLampDriver implements ActuatingDeviceDriver, ManagedService {

    private static final Logger logger = LoggerFactory.getLogger(
            PlainLampDriver.class);
    private static final String DRIVER_NAME = "Plain Lamp (Mock) Driver";
    private final Configuration configuration = new Configuration();
    private final DriverInformation info = new DriverInformation(
            Activator.DRIVER_PID,
            URI.create("https://raw.githubusercontent.com/semiotproject/semiot-platform/" +
                    "support_actuators/device-proxy-service-drivers/mock-plain-lamp/" +
                    "src/main/resources/ru.semiot.drivers.mocks.plainlamp/prototype.ttl#"));

    private volatile DeviceDriverManager manager;

    public void start() {
        manager.registerDriver(info);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            manager.registerDevice(info, new PlainLamp("123123123123"));
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }

        logger.info("{} started!", DRIVER_NAME);
    }

    public void stop() {
        logger.info("{} stopped!", DRIVER_NAME);
    }

    public void updated(Dictionary dictionary) throws ConfigurationException {
        synchronized (this) {
            if (dictionary != null) {
                if (configuration.isConfigured()) {
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

    public void executeCommand(String command) {
        logger.info("Received executeCommand: {}", command);
    }
}
