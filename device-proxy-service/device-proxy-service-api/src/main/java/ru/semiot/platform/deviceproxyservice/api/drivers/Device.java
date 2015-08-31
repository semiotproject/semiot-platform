package ru.semiot.platform.deviceproxyservice.api.drivers;

public class Device {
    
    private String id;
    
    public Device() {
        this.id = "-1";
    }
    
    public Device(String id) {
        this.id = id;
    }
    
    public String getID() {
        return id;
    }
    
}
