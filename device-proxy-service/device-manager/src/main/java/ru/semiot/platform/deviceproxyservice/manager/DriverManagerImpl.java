package ru.semiot.platform.deviceproxyservice.manager;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFLanguages;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.*;
import ru.semiot.platform.deviceproxyservice.api.drivers.CommandExecutionException;
import ws.wamp.jawampa.WampClient;

import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;

public class DriverManagerImpl implements DeviceDriverManager, ManagedService {

    private static final Logger logger = LoggerFactory.getLogger(
            DriverManagerImpl.class);
    private final Configuration configuration = new Configuration();

    //Injected by Dependency Manager
    private BundleContext bundleContext;

    private DirectoryService directoryService;

    public DriverManagerImpl() {

    }

    public void start() {
        logger.info("Device Manager is starting...");
        try {
            directoryService = new DirectoryService(new RDFStore(configuration));

            logger.debug("Directory service is ready");


            WAMPClient
                    .getInstance()
                    .init(configuration.get(Keys.WAMP_URI),
                            configuration.get(Keys.WAMP_REALM),
                            configuration.getAsInteger(Keys.WAMP_RECONNECT),
                            configuration.get(Keys.WAMP_LOGIN),
                            configuration.get(Keys.WAMP_PASSWORD))
                    .subscribe(
                            (WampClient.State newState) -> {
                                if (newState instanceof WampClient.ConnectedState) {
                                    logger.info("Connected to {}", configuration.get(Keys.WAMP_URI));
                                } else if (newState instanceof WampClient.DisconnectedState) {
                                    logger.info("Disconnected from {}. Reason: {}",
                                            configuration.get(Keys.WAMP_URI),
                                            ((WampClient.DisconnectedState) newState).disconnectReason());
                                } else if (newState instanceof WampClient.ConnectingState) {
                                    logger.info("Connecting to {}", configuration.get(Keys.WAMP_URI));
                                }
                            });
            logger.info("Device Proxy Service Manager started!");
        } catch (Throwable ex) {
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
                    configuration.put(Keys.WAMP_LOGIN, "internal");
                    configuration.put(Keys.WAMP_PASSWORD, "internal");
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
                    configuration.put(Keys.PLATFORM_SUBSYSTEM_PATH, "subsystems");

                    configuration.putAll(dictionary);

                    configuration.put(Keys.PLATFORM_SYSTEMS_URI_PREFIX,
                            configuration.get(Keys.PLATFORM_DOMAIN) + "/"
                                    + configuration.get(Keys.PLATFORM_SYSTEMS_PATH));

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
            logger.debug("Device [{}] is being registered", device.getId());

            /**
             * Resolve common variables, e.g. platform's domain name.
             */
            final String description = TemplateUtils.resolve(
                    device.toTurtleString(), configuration);

            boolean isAdded = directoryService.addNewDevice(info, device, description);

            if (isAdded) {
                logger.info("Device [{}] was registered!", device.getId());
                WAMPClient.getInstance().publish(
                        getConfiguration().get(Keys.TOPIC_NEWANDOBSERVING),
                        description).subscribe(WAMPClient.onError());
            } else {
                logger.warn("Device [{}] was not added in database!");
            }
        } else {
            logger.error("DirectoryService hasn't been initialized!");
        }
    }

    @Override
    public void registerObservation(Device device, Observation observation) {
        logger.info("Observation [ID={}] is being registered", device.getId());
        // TODO: There's no guarantee that WAMPClient is connected.
        WAMPClient.getInstance().publish(device.getId(),
                observation.toObservationAsString(device.getProperties(),
                        configuration, RDFLanguages.JSONLD))
                .subscribe(WAMPClient.onError());
    }

    @Override
    public void removeDataOfDriverFromFuseki(String pid) {
        directoryService.removeDataOfDriver(pid);
    }

    @Override
    public void registerCommand(CommandExecutionResult result) {
        //TODO: There's no guarantee that WAMPClient is connected?
        WAMPClient.getInstance().publish(result.getDevice().getId(),
                result.toActuationAsString(configuration, RDFLanguages.JSONLD))
                .subscribe(WAMPClient.onError());
    }

    @Override
    public Model executeCommand(String deviceId, Model command)
            throws CommandExecutionException {
        try {
            String pid = directoryService.findDriverPidByDeviceId(deviceId);

            if (pid != null) {
                try {
                    Collection services = bundleContext.getServiceReferences(
                            ActuatingDeviceDriver.class, "(service.pid=" + pid + ")");
                    if (!services.isEmpty()) {
                        logger.info("Driver [{}] found!", pid);
                        ServiceReference<ActuatingDeviceDriver> reference =
                                (ServiceReference) services.iterator().next();
                        ActuatingDeviceDriver driver = bundleContext.getService(reference);

                        CommandExecutionResult result = driver.executeCommand(command);

                        return result.toActuationAsModel(configuration);
                    } else {
                        throw CommandExecutionException.driverNotFound();
                    }
                } catch (InvalidSyntaxException e) {
                    throw CommandExecutionException.driverNotFound(e);
                }
            } else {
                throw CommandExecutionException.driverNotFound();
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);

            return null;
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

}
