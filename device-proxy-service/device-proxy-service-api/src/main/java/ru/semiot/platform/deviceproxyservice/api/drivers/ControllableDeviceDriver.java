package ru.semiot.platform.deviceproxyservice.api.drivers;

public interface ControllableDeviceDriver extends DeviceDriver {

  public RDFTemplate getRDFTemplate(String id);

  public CommandResult executeCommand(Command command) throws CommandExecutionException;

}
