package ru.semiot.platform.deviceproxyservice.manager;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.utils.JsonUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFLanguages;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.rdf.ModelJsonLdUtils;
import ru.semiot.platform.deviceproxyservice.api.drivers.ActuatingDeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.CommandExecutionException;
import ru.semiot.platform.deviceproxyservice.api.drivers.CommandExecutionResult;
import ru.semiot.platform.deviceproxyservice.api.drivers.Configuration;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriverManager;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceProperties;
import ru.semiot.platform.deviceproxyservice.api.drivers.DriverInformation;
import ru.semiot.platform.deviceproxyservice.api.drivers.Observation;
import ws.wamp.jawampa.WampClient;

import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;

public class DriverManagerImpl implements DeviceDriverManager, ManagedService {

  private static final Logger logger = LoggerFactory.getLogger(DriverManagerImpl.class);
  private static final String FRAME_PATH_PREFIX = "/ru/semiot/platform/deviceproxyservice/manager/";
  private static final String OBSERVATION_FRAME_PATH = FRAME_PATH_PREFIX +
      "Observation-frame.jsonld";
  private static final String SYSTEM_FRAME_PATH = FRAME_PATH_PREFIX + "System-frame.jsonld";
  private static final String TOPIC_OBSERVATIONS = "${SYSTEM_ID}.observations.${SENSOR_ID}";
  private static final String TOPIC_ACTUATIONS = "${SYSTEM_ID}.commandresults";
  private final Configuration configuration = new Configuration();
  private final Object observationFrame;
  private final Object systemFrame;

  //Injected by Dependency Manager
  private BundleContext bundleContext;

  private DirectoryService directoryService;

  public DriverManagerImpl() {
    //Loading JSONLD frame for observations
    Object frame = null;
    try {
      frame = JsonUtils.fromInputStream(
          this.getClass().getResourceAsStream(OBSERVATION_FRAME_PATH));
    } catch (Throwable e) {
      logger.error(e.getMessage(), e);
    }
    this.observationFrame = frame;

    //Loadin JSONLD frame for systems
    try {
      frame = JsonUtils.fromInputStream(
          this.getClass().getResourceAsStream(SYSTEM_FRAME_PATH));
    } catch (Throwable e) {
      logger.error(e.getMessage(), e);
    }
    this.systemFrame = frame;
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
      final Model description = device.toDescriptionAsModel(configuration);

      boolean isAdded = directoryService.addNewDevice(info, device, description);

      if (isAdded) {
        try {
          logger.info("Device [{}] was registered!", device.getId());
          String message = JsonUtils.toString(
              ModelJsonLdUtils.toJsonLdCompact(description, systemFrame));
          WAMPClient.getInstance().publish(
              getConfiguration().get(Keys.TOPIC_NEWANDOBSERVING), message)
              .subscribe(WAMPClient.onError());
        } catch (JsonLdError | IOException ex) {
          logger.error(ex.getMessage(), ex);
        }
      } else {
        logger.warn("Device [{}] was not added in database!");
      }
    } else {
      logger.error("DirectoryService hasn't been initialized!");
    }
  }

  @Override
  public void registerObservation(Device device, Observation observation) {
    logger.info("Observation [Device ID={}] is being registered", device.getId());
    // TODO: There's no guarantee that WAMPClient is connected.
    Model model = observation.toObservationAsModel(device.getProperties(), configuration);
    try {
      WAMPClient.getInstance()
          .publish(TOPIC_OBSERVATIONS.replace("${SYSTEM_ID}", device.getId())
              .replace("${SENSOR_ID}", observation.getProperty(DeviceProperties.SENSOR_ID)),
              JsonUtils.toString(ModelJsonLdUtils.toJsonLdCompact(model, observationFrame)))
          .subscribe(WAMPClient.onError());
    } catch (JsonLdError | IOException ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  @Override
  public void removeDataOfDriverFromFuseki(String pid) {
    directoryService.removeDataOfDriver(pid);
  }

  @Override
  public void registerCommand(CommandExecutionResult result) {
    //TODO: There's no guarantee that WAMPClient is connected?
    WAMPClient.getInstance()
        .publish(TOPIC_ACTUATIONS.replace("${SYSTEM_ID}", result.getDevice().getId()),
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
