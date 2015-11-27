package ru.semiot.platform.deviceproxyservice.api.drivers;

public class Device {
    
    private String id;
    private String rdfDescription;
    private boolean turnOn = true;
    
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
    
    public boolean getTurnOn() {
    	return turnOn;
    }
    
    public void setID(String id) {
    	this.id = id;
    }
    
    public void setRDFDescription(String rdfDescription) {
    	this.rdfDescription = rdfDescription;
    }
    
    public void setTurnOn(boolean turnOn) {
    	this.turnOn = turnOn;
    }
    

    @Override
    public boolean equals(Object object)
    {
        if (this.id!=null && object != null && object instanceof Device)
        {
            return this.id.equals(((Device) object).getID());
        }

        return false;
    }
}
