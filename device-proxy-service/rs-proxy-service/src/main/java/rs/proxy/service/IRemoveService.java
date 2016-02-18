package rs.proxy.service;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("remove")
public interface IRemoveService {
 
  @GET
  @Path("fuseki/{pid}")
  @Produces(MediaType.TEXT_PLAIN)
  int fuseki(@PathParam("pid") String name);
}
