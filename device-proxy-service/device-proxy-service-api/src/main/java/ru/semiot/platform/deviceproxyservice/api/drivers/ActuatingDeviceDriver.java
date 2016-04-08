package ru.semiot.platform.deviceproxyservice.api.drivers;

public interface ActuatingDeviceDriver extends DeviceDriver {

    public void executeCommand(String command) throws CommandExecutionException;

}
