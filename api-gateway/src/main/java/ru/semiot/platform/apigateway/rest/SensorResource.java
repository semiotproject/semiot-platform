package ru.semiot.platform.apigateway.rest;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/sensors")
public class SensorResource {

    @Context
    private UriInfo context;

    public SensorResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_LD_JSON)
    public void listSensors() {
        
    }
    
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_LD_JSON)
    public void getSensor() {
    
    }

}
