package ru.semiot.platform.deviceproxyservice.api.drivers;

public interface DeviceManager {

    public void registerDriver(DriverInformation info);
    
    public void registerDevice(DriverInformation info, Device device);
    
    public void updateDevice(Device device);
    
    public void registerObservation(Device device, Observation observation);
    
    public void removeDataOfDriverFromFuseki(String pid);
    
}
