package ru.semiot.platform.deviceproxyservice.api.drivers;

import java.util.List;

public interface ControllableDeviceDriver extends DeviceDriver {

  public CommandResult executeCommand(Command command) throws CommandExecutionException;

}
