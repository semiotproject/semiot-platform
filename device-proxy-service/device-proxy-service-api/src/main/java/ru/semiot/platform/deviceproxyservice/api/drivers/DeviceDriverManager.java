package ru.semiot.platform.deviceproxyservice.api.drivers;

public interface DeviceDriverManager {

    public void registerDriver(DriverInformation info);
    
    public void registerDevice(DriverInformation info, Device device);

    public void updateDevice(Device device);

    /**
     * It's called by a device driver.
     *
     * @param device a device observed the given observation
     * @param observation an observation
     */
    public void registerObservation(Device device, Observation observation);

    /**
     * It's called by a device driver.
     *
     * @param command a command performed on a device
     */
    public void registerCommand(Command command);

    public void executeCommand(String systemId, String command);
    
    public void removeDataOfDriverFromFuseki(String pid);
    
}
