package ru.semiot.platform.apigateway.rest;

import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/observations")
public class ObservationsResource {

    private static final Logger logger = LoggerFactory.getLogger(ObservationsResource.class);
    
    public ObservationsResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_LD_JSON)
    public void allByIntervalAndSensor(
            @Suspended final AsyncResponse response, 
            @QueryParam("start") String start, 
            @QueryParam("end") String end,
            @QueryParam("sensor_uri") String systemId,
            @QueryParam("property") String property) {
        // /observation?start=&end=&sensor_uri=&property=
        
        
    }

}
