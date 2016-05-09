package ru.semiot.drivers.mocks.plainlamp;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.Command;
import ru.semiot.platform.deviceproxyservice.api.drivers.CommandExecutionException;
import ru.semiot.platform.deviceproxyservice.api.drivers.CommandResult;
import ru.semiot.platform.deviceproxyservice.api.drivers.Configuration;
import ru.semiot.platform.deviceproxyservice.api.drivers.ControllableDeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriverManager;
import ru.semiot.platform.deviceproxyservice.api.drivers.DriverInformation;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlainLampDriver implements ControllableDeviceDriver, ManagedService {

  private static final Logger logger = LoggerFactory.getLogger(PlainLampDriver.class);
  private static final String PROTOTYPE_URI_PREFIX =
      "https://raw.githubusercontent.com/semiotproject/semiot-platform/"
          + "master/device-proxy-service-drivers/mock-plain-lamp/"
          + "src/main/resources/ru/semiot/drivers/mocks/plainlamp/prototype.ttl#";
  private static final String PROCESS_IDLE = PROTOTYPE_URI_PREFIX + "PlainLamp-Idle";
  private static final String PROCESS_SHINE = PROTOTYPE_URI_PREFIX + "PlainLamp-Shine";
  private static final String PARAM_SHINE_LUMEN = PROTOTYPE_URI_PREFIX + "PlainLamp-Shine-Lumen";
  private static final String PARAM_SHINE_COLOR = PROTOTYPE_URI_PREFIX + "PlainLamp-Shine-Color";
  private static final String DRIVER_NAME = "Plain Lamp (Mock) Driver";
  private static final String DEVICE_ID_PREFIX = "123123123123123";

  private final Configuration configuration = new Configuration();
  private final DriverInformation info =
      new DriverInformation(Activator.DRIVER_PID, URI.create(PROTOTYPE_URI_PREFIX + "PlainLamp"));
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  private Map<String, PlainLamp> lamps = new HashMap<>();

  private volatile DeviceDriverManager manager;

  public void start() {
    try {
      manager.registerDriver(info);

      logger.info("{} started!", DRIVER_NAME);

      Random random = new Random();
      int switchOnOffInterval = configuration.getAsInteger(ConfigurationKeys.SWITCH_ONOFF_INTERVAL);

      for (int i = 0; i < configuration.getAsInteger(ConfigurationKeys.NUMBER_OF_LAMPS); i++) {
        PlainLamp lamp = new PlainLamp(DEVICE_ID_PREFIX + i);
        String lamp_id = lamp.getId();
        lamps.put(lamp_id, lamp);

        manager.registerDevice(info, lamp);

        executor.scheduleAtFixedRate(() -> {
          try {
            PlainLamp l = lamps.get(lamp_id);
            Command command = new Command(l.getId(), Command.TYPE_SWITCHPROCESSCOMMAND);

            synchronized (l) {
              if (l.getIsOn()) {
                l.setIsOn(false);

                command.addProperty(Command.PROP_TARGETPROCESS, PROCESS_IDLE);
                logger.debug("[ID={}] Switched off!", l.getId());
              } else {
                l.setIsOn(true);

                command.addProperty(Command.PROP_TARGETPROCESS, PROCESS_SHINE);
                command.addParameter(PARAM_SHINE_LUMEN, 890);
                command.addParameter(PARAM_SHINE_COLOR, 4000);
                logger.debug("[ID={}] Switched on!", l.getId());
              }
            }

            manager.registerCommand(l, new CommandResult(command, ZonedDateTime.now()));
          } catch (Throwable e) {
            logger.error(e.getMessage(), e);
          }
        }, random.nextInt(switchOnOffInterval), switchOnOffInterval, TimeUnit.SECONDS);
      }
    } catch (Throwable e) {
      logger.error(e.getMessage(), e);
    }
  }

  public void stop() {
    lamps.clear();

    try {
      executor.shutdown();
      executor.awaitTermination(10, TimeUnit.SECONDS);
      if (!executor.shutdownNow().isEmpty()) {
        logger.warn("Some scheduled tasks were not shutdown!");
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

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

  public CommandResult executeCommand(Command command) throws CommandExecutionException {
    try {
      if (lamps.containsKey(command.getDeviceId())) {
        PlainLamp device = lamps.get(command.getDeviceId());
        String commandType = command.getCommandType();
        synchronized (device) {
          if (commandType.equals(Command.TYPE_SWITCHPROCESSCOMMAND)) {
            String targetProcess = command.getPropertyValue(Command.PROP_TARGETPROCESS);
            if (targetProcess.equals(PROCESS_IDLE)) {
              device.setIsOn(false);

              logger.debug("[ID={}] Switched off!", device.getId());
            } else if (targetProcess.equals(PROCESS_SHINE)) {
              device.setIsOn(true);

              logger.debug("[ID={}] Switched on!", device.getId());
            }
          } else {
            throw CommandExecutionException.badCommand("Command is not supported!");
          }
        }

        CommandResult result = new CommandResult(command, ZonedDateTime.now());

        manager.registerCommand(device, result);

        return result;
      } else {
        throw CommandExecutionException.systemNotFound();
      }
    } catch (Throwable e) {
      logger.error(e.getMessage(), e);

      return null;
    }
  }
}


