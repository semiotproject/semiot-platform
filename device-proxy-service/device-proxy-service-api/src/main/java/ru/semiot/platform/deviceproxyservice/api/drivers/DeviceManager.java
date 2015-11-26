package ru.semiot.platform.deviceproxyservice.api.drivers;

import rx.Observable;

public interface DeviceManager {

    public void register(Device device);
    
    public void inactiveDevice(String message);
    
    public void updateStatus(Device device, DeviceStatus status);
    
    public void publish(String topic, String message);
    
    public Observable<String> subscribe(String topic);
    
    public String getDomain();
    
    public String getPathSystemUri();
    
}
