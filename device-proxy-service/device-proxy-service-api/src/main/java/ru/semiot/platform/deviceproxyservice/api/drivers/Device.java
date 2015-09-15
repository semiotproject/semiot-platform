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

    @Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof Device)
        {
            return this.id.equals(((Device) object).getID());
        }

        return false;
    }
}
