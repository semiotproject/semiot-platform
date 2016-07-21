package ru.semiot.platform.deviceproxyservice.rest;

import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriverManager;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("remove")
public class RemoveServiceImpl {

  private DeviceDriverManager manager;

  @DELETE
  @Path("fuseki/{pid}")
  @Produces(MediaType.TEXT_PLAIN)
  public String fuseki(@PathParam("pid") String pid) {
    manager.removeDataOfDriverFromFuseki(pid);
    return "Deleted data from fuseki. Driver pid = " + pid;
  }
}