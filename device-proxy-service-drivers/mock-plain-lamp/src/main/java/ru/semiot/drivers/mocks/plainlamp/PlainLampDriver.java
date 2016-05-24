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
import ru.semiot.platform.deviceproxyservice.api.drivers.RDFTemplate;

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
          + "command_to_keyvalue/device-proxy-service-drivers/mock-plain-lamp/"
          + "src/main/resources/ru/semiot/drivers/mocks/plainlamp/prototype.ttl#";
  private static final String PROCESS_LIGHT = "light";
  private static final String COMMAND_LIGHT_STOP = "light-stopcommand";
  private static final String COMMAND_LIGHT_START = "light-startcommand";
  private static final String DRIVER_NAME = "Plain Lamp (Mock) Driver";
  private static final String DEVICE_ID_PREFIX = "123123123123123";

  private final Configuration configuration = new Configuration();
  private final DriverInformation info =
      new DriverInformation(Activator.DRIVER_PID, URI.create(PROTOTYPE_URI_PREFIX + "PlainLamp"));
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  private RDFTemplate TEMPLATE_COMMAND_LIGHT_STOP;
  private RDFTemplate TEMPLATE_COMMAND_LIGHT_START;

  private Map<String, PlainLamp> lamps = new HashMap<>();

  private volatile DeviceDriverManager manager;

  public void start() {
    try {
      loadRDFTemplates();

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

            synchronized (l) {
              if (l.getIsOn()) {
                l.setIsOn(false);

                Command command = new Command(PROCESS_LIGHT, COMMAND_LIGHT_STOP);
                command.add(PlainLampProps.PROCESS_ID, PROCESS_LIGHT);
                command.add(PlainLampProps.DEVICE_ID, l.getId());

                logger.debug("[ID={}] Switched off!", l.getId());

                manager.registerCommand(l, new CommandResult(
                    command,
                    getRDFTemplate(COMMAND_LIGHT_STOP),
                    ZonedDateTime.now()));
              } else {
                l.setIsOn(true);

                Command command = new Command(PROCESS_LIGHT, COMMAND_LIGHT_START);
                command.add(PlainLampProps.PROCESS_ID, PROCESS_LIGHT);
                command.add(PlainLampProps.DEVICE_ID, l.getId());
                command.add(PlainLampProps.PROCESS_LIGHT_PARAMETER_LUMEN, 890);
                command.add(PlainLampProps.PROCESS_LIGHT_PARAMETER_COLOR, 4000);

                logger.debug("[ID={}] Switched on!", l.getId());

                manager.registerCommand(l, new CommandResult(
                    command,
                    getRDFTemplate(COMMAND_LIGHT_START),
                    ZonedDateTime.now()));
              }
            }
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

  @Override
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

  @Override
  public String getDriverName() {
    return DRIVER_NAME;
  }

  @Override
  public RDFTemplate getRDFTemplate(String id) {
    switch (id) {
      case COMMAND_LIGHT_START:
        return TEMPLATE_COMMAND_LIGHT_START;
      case COMMAND_LIGHT_STOP:
        return TEMPLATE_COMMAND_LIGHT_STOP;
      default:
        throw new IllegalArgumentException();
    }
  }

  private void loadRDFTemplates() {
    try {
      TEMPLATE_COMMAND_LIGHT_STOP = new RDFTemplate(COMMAND_LIGHT_STOP, this.getClass()
          .getResourceAsStream("/ru/semiot/drivers/mocks/plainlamp/light-stopcommand.ttl"));
      TEMPLATE_COMMAND_LIGHT_START = new RDFTemplate(COMMAND_LIGHT_START, this.getClass()
          .getResourceAsStream("/ru/semiot/drivers/mocks/plainlamp/light-startcommand.ttl"));
    } catch (Throwable ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  @Override
  public CommandResult executeCommand(Command command) throws CommandExecutionException {
    try {
      if (lamps.containsKey(command.get(PlainLampProps.DEVICE_ID))) {
        PlainLamp device = lamps.get(command.get(PlainLampProps.DEVICE_ID));
        String commandId = command.get(PlainLampProps.COMMAND_ID);
        String processId = command.get(PlainLampProps.PROCESS_ID);
        synchronized (device) {
          if (processId.equals(PROCESS_LIGHT)) {
            if (commandId.equals(COMMAND_LIGHT_STOP)) {
              device.setIsOn(false);

              logger.debug("[ID={}] Turned off the light!", device.getId());
            } else if (commandId.equals(COMMAND_LIGHT_START)) {
              device.setIsOn(true);
              device.setLumen(command.getAsInteger(
                  PlainLampProps.PROCESS_LIGHT_PARAMETER_LUMEN));
              device.setKelvin(command.getAsInteger(
                  PlainLampProps.PROCESS_LIGHT_PARAMETER_COLOR));

              logger.debug("[ID={}] Turned on the light! Lumen: {}, Kelvin: {}",
                  device.getId(), device.getLumen(), device.getKelvin());
            } else {
              throw CommandExecutionException.badCommand(
                  "Command [%s] is not supported!", commandId);
            }
          } else {
            throw CommandExecutionException.badCommand("Process [%s] is not supported!", processId);
          }
        }

        CommandResult result = new CommandResult(
            command, getRDFTemplate(commandId), ZonedDateTime.now());
        manager.registerCommand(device, result);

        return result;
      } else {
        throw CommandExecutionException.systemNotFound();
      }
    } catch (Throwable e) {
      if (e instanceof CommandExecutionException) {
        throw e;
      } else {
        throw CommandExecutionException.badCommand(e);
      }
    }
  }
}


