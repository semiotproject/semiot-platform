package ru.semiot.services.tsdbservice.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/commands")
public class Commands {

    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/ld+json"})
    public void getCommandLatest(@Suspended AsyncResponse response) {

        response.resume(Response.ok().build());
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/ld+json"})
    public void getCommandsByRange(@Suspended AsyncResponse response) {

        response.resume(Response.ok().build());
    }
}
