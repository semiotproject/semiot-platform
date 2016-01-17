package ru.semiot.platform.deviceproxyservice.api.drivers;

import java.net.URI;

public class DriverInformation {
    
    private final String id;
    private final URI prototypeUri;
    
    public DriverInformation(String id, URI prototypeUri) {
        this.id = id;
        this.prototypeUri = prototypeUri;
    }
    
    public String getId() {
        return this.id;
    }
    
    public URI getPrototypeUri() {
        return this.prototypeUri;
    }
    
}
