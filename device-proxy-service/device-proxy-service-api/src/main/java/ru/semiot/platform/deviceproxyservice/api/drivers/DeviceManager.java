package ru.semiot.platform.deviceproxyservice.api.drivers;

import rx.Observable;

public interface DeviceManager {

    public void register(Device device);
    
    public void update(Device device);
    
    public void publish(String topic, String message);
    
    public void registerObservation(Device device, Observation observation);
    
    public Observable<String> subscribe(String topic);
    
}
