package ru.semiot.platform.deviceproxyservice.api.drivers;

public class Device {
    
    private final String id;
    private final String rdfDescription;
    
    public Device(String id, String rdfDescription) {
        this.id = id;
        this.rdfDescription = rdfDescription;
    }
    
    public String getID() {
        return id;
    }
    
    public String getRDFDescription() {
        return rdfDescription;
    }
    
}
