package ru.semiot.platform.deviceproxyservice.api.drivers;

public class Device {
    
    private String id;
    private String rdfDescription;
    
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
    
    public void setID(String id) {
    	this.id = id;
    }
    
    public void setRDFDescription(String rdfDescription) {
    	this.rdfDescription = rdfDescription;
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
