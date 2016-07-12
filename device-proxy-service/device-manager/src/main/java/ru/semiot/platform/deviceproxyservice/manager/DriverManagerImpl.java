package ru.semiot.platform.deviceproxyservice.manager;

import com.github.jsonldjava.utils.JsonUtils;
import org.apache.jena.rdf.model.Model;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.rdf.ModelJsonLdUtils;
import ru.semiot.platform.deviceproxyservice.api.drivers.Command;
import ru.semiot.platform.deviceproxyservice.api.drivers.CommandExecutionException;
import ru.semiot.platform.deviceproxyservice.api.drivers.CommandResult;
import ru.semiot.platform.deviceproxyservice.api.drivers.Configuration;
import ru.semiot.platform.deviceproxyservice.api.drivers.ControllableDeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriverManager;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceProperties;
import ru.semiot.platform.deviceproxyservice.api.drivers.DriverInformation;
import ru.semiot.platform.deviceproxyservice.api.drivers.Observation;
import ru.semiot.platform.deviceproxyservice.api.drivers.RDFTemplate;
import ru.semiot.platform.deviceproxyservice.api.manager.CommandFactory;
import ru.semiot.platform.deviceproxyservice.api.manager.DirectoryService;
import ws.wamp.jawampa.WampClient;

import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DriverManagerImpl implements DeviceDriverManager, ManagedService {

  private static final Logger logger = LoggerFactory.getLogger(DriverManagerImpl.class);
  private static final String FRAME_PATH_PREFIX = "/ru/semiot/platform/deviceproxyservice/manager/";
  private static final String OBSERVATION_FRAME_PATH =
      FRAME_PATH_PREFIX + "Observation-frame.jsonld";
  private static final String COMMANDRESULT_FRAME_PATH =
      FRAME_PATH_PREFIX + "CommandResult-frame.jsonld";
  private static final String SYSTEM_FRAME_PATH = FRAME_PATH_PREFIX + "System-frame.jsonld";
  private static final String VAR_SYSTEM_ID = "${SYSTEM_ID}";
  private static final String VAR_PROCESS_ID = "${PROCESS_ID}";
  private static final String TOPIC_OBSERVATIONS = "${SYSTEM_ID}.observations.${SENSOR_ID}";
  private static final String TOPIC_COMMANDRESULT = "${SYSTEM_ID}.commandresults.${PROCESS_ID}";
  private final Configuration configuration = new Configuration();
  private final Object observationFrame;
  private final Object systemFrame;
  private final Object commandResultFrame;

  //Injected by Dependency Manager
  private BundleContext bundleContext;

  //Injected by Dependency Manager
  private DirectoryService directoryService;

  private ExecutorService executor = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS,
      new SynchronousQueue(), new ThreadPoolExecutor.CallerRunsPolicy());

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

    //Loading JSONLD frame for systems
    try {
      frame = JsonUtils.fromInputStream(
          this.getClass().getResourceAsStream(SYSTEM_FRAME_PATH));
    } catch (Throwable e) {
      logger.error(e.getMessage(), e);
    }
    this.systemFrame = frame;

    try {
      frame = JsonUtils.fromInputStream(
          this.getClass().getResourceAsStream(COMMANDRESULT_FRAME_PATH));
    } catch (Throwable e) {
      logger.error(e.getMessage(), e);
    }
    this.commandResultFrame = frame;
  }

  public void start() {
    logger.info("Device Manager is starting...");
    try {
      WAMPClient
          .getInstance()
          .init(configuration.getAsString(Keys.WAMP_URI),
              configuration.getAsString(Keys.WAMP_REALM),
              configuration.getAsInteger(Keys.WAMP_RECONNECT),
              configuration.getAsString(Keys.WAMP_LOGIN),
              configuration.getAsString(Keys.WAMP_PASSWORD))
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
      executor.shutdown();
      executor.awaitTermination(30, TimeUnit.SECONDS);
      executor.shutdownNow();
    } catch (Throwable ex) {
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
          configuration.put(Keys.TOPIC_NEWANDOBSERVING, "ru.semiot.devices.newandobserving");
          configuration.put(Keys.TOPIC_INACTIVE, "ru.semiot.devices.turnoff");
          configuration.put(Keys.FUSEKI_PASSWORD, "pw");
          configuration.put(Keys.FUSEKI_USERNAME, "admin");
          configuration.put(Keys.FUSEKI_UPDATE_URL, "http://triplestore:3030/blazegraph/sparql");
          configuration.put(Keys.FUSEKI_QUERY_URL, "http://triplestore:3030/blazegraph/sparql");
          configuration.put(Keys.FUSEKI_STORE_URL, "http://triplestore:3030/blazegraph/sparql");
          configuration.put(Keys.PLATFORM_DOMAIN, "http://localhost");
          configuration.put(Keys.PLATFORM_SYSTEMS_PATH, "systems");
          configuration.put(Keys.PLATFORM_SUBSYSTEM_PATH, "subsystems");
          configuration.put(Keys.PLATFORM_PROCESS_PATH, "processes");

          configuration.putAll(dictionary);

          //Composite properties
          configuration.put(Keys.PLATFORM_SYSTEMS_URI_PREFIX,
              configuration.get(Keys.PLATFORM_DOMAIN) + "/"
                  + configuration.get(Keys.PLATFORM_SYSTEMS_PATH));
          configuration.put(Keys.PLATFORM_PROCESS_URI_PREFIX,
              configuration.get(Keys.PLATFORM_SYSTEMS_URI_PREFIX) + "/{{"
                  + DeviceProperties.DEVICE_ID + "}}/"
                  + configuration.get(Keys.PLATFORM_PROCESS_PATH));

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
    directoryService.loadDevicePrototype(info.getPrototypeUri());
  }

  @Override
  public void updateDevice(Device device) {
    //TODO: Here we should update device's states.
  }

  @Override
  public void registerDevice(DriverInformation info, Device device) {
    executor.execute(() -> {
      logger.debug("Device [{}] is being registered", device.getId());

      try {
        /**
         * Resolve common variables, e.g. platform's domain name.
         */
        final Model description = device.toDescriptionAsModel(configuration);

        boolean isAdded = directoryService.addNewDevice(info, device, description);

        if (isAdded) {

          logger.info("Device [{}] was registered!", device.getId());
          String message = JsonUtils.toString(
              ModelJsonLdUtils.toJsonLdCompact(description, systemFrame));
          WAMPClient.getInstance().publish(
              getConfiguration().getAsString(Keys.TOPIC_NEWANDOBSERVING), message)
              .subscribe(WAMPClient.onError());
        } else {
          logger.warn("Device [{}] was not added in database!");
        }
      } catch (Throwable ex) {
        logger.error(ex.getMessage(), ex);
      }
    });
  }

  @Override
  public void registerObservation(Device device, Observation observation) {
    executor.execute(() -> {
      logger.info("Observation [Device ID={}] is being registered", device.getId());
      // TODO: There's no guarantee that WAMPClient is connected.
      Model model = observation.toObservationAsModel(device.getProperties(), configuration);
      try {
        WAMPClient.getInstance()
            .publish(TOPIC_OBSERVATIONS.replace("${SYSTEM_ID}", device.getId())
                    .replace("${SENSOR_ID}", observation.getProperty(DeviceProperties.SENSOR_ID)),
                JsonUtils.toString(ModelJsonLdUtils.toJsonLdCompact(model, observationFrame)))
            .subscribe(WAMPClient.onError());
      } catch (Throwable ex) {
        logger.error(ex.getMessage(), ex);
      }
    });
  }

  @Override
  public void removeDataOfDriverFromFuseki(String pid) {
    // directoryService.removeDataOfDriver(pid);
  }

  @Override
  public void registerCommand(Device device, CommandResult result) {
    executor.execute(() -> {
      try {
        Model model = result.toRDFAsModel(configuration);
        //TODO: There's no guarantee that WAMPClient is connected?
        WAMPClient.getInstance().publish(
            TOPIC_COMMANDRESULT
                .replace(VAR_SYSTEM_ID, device.getId())
                .replace(VAR_PROCESS_ID, result.get(DeviceProperties.PROCESS_ID)),
            JsonUtils.toString(ModelJsonLdUtils.toJsonLdCompact(model, commandResultFrame)))
            .subscribe(WAMPClient.onError());
      } catch (Throwable e) {
        logger.error(e.getMessage(), e);
      }
    });
  }

  @Override
  public Model executeCommand(String deviceId, Model commandModel)
      throws CommandExecutionException {
    try {
      String pid = directoryService.findDriverPidByDeviceId(deviceId);

      if (pid != null) {
        try {
          Collection services = bundleContext.getServiceReferences(
              ControllableDeviceDriver.class, "(service.pid=" + pid + ")");
          if (!services.isEmpty()) {
            logger.debug("Driver [{}] found!", pid);
            ServiceReference<ControllableDeviceDriver> reference =
                (ServiceReference) services.iterator().next();
            ControllableDeviceDriver driver = bundleContext.getService(reference);

            String commandId = CommandFactory.extractCommandId(commandModel);
            RDFTemplate template = driver.getRDFTemplate(commandId);
            Command command = CommandFactory.buildCommand(commandModel, template);
            CommandResult result = driver.executeCommand(command);

            return result.toRDFAsModel(configuration);
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
      if (e instanceof CommandExecutionException) {
        throw e;
      } else {
        throw CommandExecutionException.badCommand(e);
      }
    }
  }

  public Configuration getConfiguration() {
    return configuration;
  }

}
