package ru.semiot.drivers.mocks.plainlamp;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFLanguages;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.DUL;
import ru.semiot.commons.namespaces.NamespaceUtils;
import ru.semiot.commons.namespaces.SEMIOT;
import ru.semiot.platform.deviceproxyservice.api.drivers.ActuatingDeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.Configuration;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriverManager;
import ru.semiot.platform.deviceproxyservice.api.drivers.DriverInformation;
import ru.semiot.platform.deviceproxyservice.api.drivers.CommandExecutionException;

import java.io.StringReader;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class PlainLampDriver implements ActuatingDeviceDriver, ManagedService {

    private static final Logger logger = LoggerFactory.getLogger(
            PlainLampDriver.class);
    private static final String DRIVER_NAME = "Plain Lamp (Mock) Driver";
    private static final String DEVICE_ID_PREFIX = "123123123123123";
    private final Configuration configuration = new Configuration();
    private final DriverInformation info = new DriverInformation(
            Activator.DRIVER_PID,
            URI.create("https://raw.githubusercontent.com/semiotproject/semiot-platform/" +
                    "support_actuators/device-proxy-service-drivers/mock-plain-lamp/" +
                    "src/main/resources/ru.semiot.drivers.mocks.plainlamp/prototype.ttl#"));
    private Map<String, PlainLamp> devices = new HashMap<>();

    private volatile DeviceDriverManager manager;

    public void start() {
        configuration.put(ConfigurationKeys.NUMBER_OF_LAMPS, "5");
        manager.registerDriver(info);

        logger.info("{} started!", DRIVER_NAME);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            for (int i = 0; i < configuration.getAsInteger(
                    ConfigurationKeys.NUMBER_OF_LAMPS); i++) {
                PlainLamp lamp = new PlainLamp(DEVICE_ID_PREFIX + i);
                devices.put(lamp.getId(), lamp);
                manager.registerDevice(info, lamp);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void stop() {
        devices.clear();

        logger.info("{} stopped!", DRIVER_NAME);
    }

    public void updated(Dictionary dictionary) throws ConfigurationException {
        synchronized (this) {
            if (dictionary != null) {
                if (!configuration.isConfigured()) {
                    configuration.putAll(dictionary);
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

    public void executeCommand(String command) throws CommandExecutionException {
        logger.info("Received executeCommand: {}", command);

        //Algorithm:
        // 1. Check whether the command is supported
        // 1. Get the device id
        // 1. Run the command against the given device id
        // 1. Notify others about success or failure

        Model model = ModelFactory.createDefaultModel()
                .read(new StringReader(command), null, RDFLanguages.strLangTurtle);
        NodeIterator deviceIterator = model.listObjectsOfProperty(DUL.involvesAgent);
        NodeIterator operationIterator = model.listObjectsOfProperty(SEMIOT.targetOperation);

        if (deviceIterator.hasNext() && operationIterator.hasNext()) {
            Resource deviceUri = (Resource) deviceIterator.next();
            Resource operationUri = (Resource) operationIterator.next();

            String deviceId = NamespaceUtils.extractLocalName(deviceUri.getURI());

            if (devices.containsKey(deviceId)) {
                PlainLamp device = devices.get(deviceId);

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }

                manager.registerCommand(device,
                        new SwitchOperationCommand(deviceId,
                                ZonedDateTime.now(), operationUri));
            } else {
                throw CommandExecutionException.systemNotFound();
            }
        } else {
            throw CommandExecutionException.badCommand();
        }
    }
}
