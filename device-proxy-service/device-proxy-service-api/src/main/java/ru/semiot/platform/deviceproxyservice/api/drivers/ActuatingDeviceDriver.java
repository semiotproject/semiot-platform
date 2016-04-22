package ru.semiot.platform.deviceproxyservice.api.drivers;

import org.apache.jena.rdf.model.Model;

public interface ActuatingDeviceDriver extends DeviceDriver {

  public CommandExecutionResult executeCommand(Model command)
      throws CommandExecutionException;

}
