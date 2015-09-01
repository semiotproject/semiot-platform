package ru.semiot.platform.deviceproxyservice.api.drivers;

public interface DeviceManager {

    public void register(Device device);
    
    public void updateStatus(Device device, DeviceStatus status);

}
