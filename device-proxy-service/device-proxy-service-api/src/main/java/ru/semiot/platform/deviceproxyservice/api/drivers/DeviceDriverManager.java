package ru.semiot.platform.deviceproxyservice.api.drivers;

import org.apache.jena.rdf.model.Model;

public interface DeviceDriverManager {

  public void registerDriver(DriverInformation info);

  public void registerDevice(DriverInformation info, Device device);

  public void updateDevice(Device device);

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
   * @param commandExecutionResult a command performed on a device
   */
  public void registerCommand(Device device, CommandResult commandExecutionResult);

  public Model executeCommand(String systemId, Model command)
      throws CommandExecutionException;

  public void removeDataOfDriverFromFuseki(String pid);

}
