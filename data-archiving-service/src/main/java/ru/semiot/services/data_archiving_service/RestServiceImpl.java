package ru.semiot.services.data_archiving_service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

 
@Path("/remove")
public class RestServiceImpl {
 
    @GET
    @Path("metric/{uid}")
    @Produces(MediaType.TEXT_PLAIN)
    public String removeMetric(@PathParam("uid") String uid) {
    	String res = TsdbQueryUtil.deleteMetricRequest(uid);
    	return res == null ? "Request was completed with an error" : res;
    }
}
