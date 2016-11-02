package ru.semiot.platform.deviceproxyservice.api.drivers;

import org.apache.jena.rdf.model.Model;
import ru.semiot.platform.deviceproxyservice.api.drivers.CommandExecutionException;
import ru.semiot.platform.deviceproxyservice.api.drivers.CommandResult;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DriverInformation;
import ru.semiot.platform.deviceproxyservice.api.drivers.Observation;

public interface DeviceDriverManager {

  public void registerDriver(DriverInformation info);

  public void registerDevice(DriverInformation info, Device device);

  public void updateDevice(DriverInformation info, Device device);

  /**
   * It's called by a device driver.
   *
   * @param device      a device observed the given observation
   * @param observation an observation
   */
  public void registerObservation(Device device, Observation observation);

  /**
   * It's called by a device driver.
   *
   * @param device a device which performed the command
   * @param result the result of a command performed on the device
   */
  public void registerCommand(Device device, CommandResult result);

  public Model executeCommand(String systemId, Model command)
      throws CommandExecutionException;

  public void removeDataOfDriverFromFuseki(String pid);

}
