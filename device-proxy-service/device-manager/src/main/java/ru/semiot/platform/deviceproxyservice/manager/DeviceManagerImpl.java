package ru.semiot.platform.deviceproxyservice.manager;

import java.io.IOException;
import java.util.Dictionary;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.Configuration;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;
import ru.semiot.platform.deviceproxyservice.api.drivers.DriverInformation;
import ru.semiot.platform.deviceproxyservice.api.drivers.Observation;
import ru.semiot.platform.deviceproxyservice.api.drivers.TemplateUtils;
import ws.wamp.jawampa.WampClient;

public class DeviceManagerImpl implements DeviceManager, ManagedService {

    private static final Logger logger = LoggerFactory.getLogger(
            DeviceManagerImpl.class);

    private final Configuration configuration = new Configuration();

    private DirectoryService directoryService;

    public void start() {
        logger.info("Device Manager is starting...");

        directoryService = new DirectoryService(new RDFStore(configuration));

        logger.debug("Directory service is ready");

        try {
            WAMPClient
                    .getInstance()
                    .init(configuration.get(Keys.WAMP_URI),
                            configuration.get(Keys.WAMP_REALM),
                            configuration.getAsInteger(Keys.WAMP_RECONNECT))
                    .subscribe(
                            (WampClient.State newState) -> {
                                if (newState instanceof WampClient.ConnectedState) {
                                    logger.info("Connected to {}", configuration.get(Keys.WAMP_URI));
                                } else if (newState instanceof WampClient.DisconnectedState) {
                                    logger.info("Disconnected from {}", configuration.get(Keys.WAMP_URI));
                                } else if (newState instanceof WampClient.ConnectingState) {
                                    logger.info("Connecting to {}", configuration.get(Keys.WAMP_URI));
                                }
                            });
            logger.info("Device Proxy Service Manager started!");
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            try {
                WAMPClient.getInstance().close();
            } catch (IOException ex1) {
                logger.error(ex1.getMessage(), ex1);
            }
        }
    }

    public void stop() {
        try {
            WAMPClient.getInstance().close();
            directoryService = null;
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
        logger.info("Device Proxy Service Manager stopped!");
    }

    @Override
    public void updated(Dictionary dictionary) throws ConfigurationException {
        synchronized (this) {
            if (dictionary != null) {
                if (!configuration.isConfigured()) {
                	// default values
					configuration.put(Keys.WAMP_URI, "ws://wamprouter:8080/ws");
					configuration.put(Keys.WAMP_REALM, "realm1");
					configuration.put(Keys.WAMP_RECONNECT, "15");
					configuration.put(Keys.TOPIC_NEWANDOBSERVING,
							"ru.semiot.devices.newandobserving");
					configuration.put(Keys.TOPIC_INACTIVE,
							"ru.semiot.devices.turnoff");
					configuration.put(Keys.FUSEKI_PASSWORD, "pw");
					configuration.put(Keys.FUSEKI_USERNAME, "admin");
					configuration.put(Keys.FUSEKI_UPDATE_URL,
							"http://localhost:3030/ds/update");
					configuration.put(Keys.FUSEKI_QUERY_URL,
							"http://localhost:3030/ds/query");
					configuration.put(Keys.FUSEKI_STORE_URL,
							"http://localhost:3030/ds/data");
					configuration.put(Keys.PLATFORM_DOMAIN, "http://localhost");
					configuration.put(Keys.PLATFORM_SYSTEMS_PATH, "systems");
					configuration.put(Keys.PLATFORM_SENSORS_PATH, "sensors");
                	
                    configuration.putAll(dictionary);

                    configuration.put(Keys.PLATFORM_SYSTEMS_URI_PREFIX,
                            configuration.get(Keys.PLATFORM_DOMAIN) + "/"
                            + configuration.get(Keys.PLATFORM_SYSTEMS_PATH));
                    configuration.put(Keys.PLATFORM_SENSORS_URI_PREFIX,
                            configuration.get(Keys.PLATFORM_DOMAIN) + "/"
                            + configuration.get(Keys.PLATFORM_SENSORS_PATH));

                    configuration.setConfigured();

                    logger.debug("Manager was configured");
                } else {
                    logger.warn("Manager is already configured! Ignoring it");
                }
            } else {
                logger.debug("Configuration is empty. Skipping it");
            }
        }
    }

    @Override
    public void registerDriver(DriverInformation info) {
        if (directoryService != null) {
            directoryService.addDevicePrototype(info.getPrototypeUri());
        } else {
            logger.error("DirectoryService hasn't been initialized!");
        }
    }

    @Override
    public void updateDevice(Device device) {
        //TODO: Here we should update device's states.
    }

    @Override
    public void registerDevice(DriverInformation info, Device device) {
        if (directoryService != null) {
            logger.info("Device [ID={}] is being registered", device.getId());

            /**
             * Resolve common variables, e.g. platform's domain name.
             */
            final String description = TemplateUtils.resolve(
                    device.toTurtleString(), configuration);

            boolean isAdded = directoryService.addNewDevice(info, device, description);

            if (isAdded) {
                WAMPClient.getInstance().publish(
                        getConfiguration().get(Keys.TOPIC_NEWANDOBSERVING),
                        description);
            }
        } else {
            logger.error("DirectoryService hasn't been initialized!");
        }
    }

    @Override
    public void registerObservation(Device device, Observation observation) {
        //TODO: Can we get rid of this null check?
        if (directoryService != null) {
            logger.info("Observation [ID={}] is being registered", device.getId());

            String description = TemplateUtils.resolve(
                    observation.toTurtleString(),
                    device.getProperties(),
                    configuration);

            //TODO: There's no garantee that WAMPClient is connected.
            WAMPClient.getInstance().publish(device.getId(), description);
        } else {
            logger.error("DirectoryService hasn't been initialized!");
        }
    }

    @Override
    public void removeDataOfDriverFromFuseki(String pid) {
    	directoryService.removeDataOfDriver(pid);
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }

}
