package ru.semiot.web;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
@Stateless
@Path("rest")
public class REST {
    
    @Inject
    private DataBase db;
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void create(String request){
        db.appendRequest(request);
    }
    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String count(){
        return Integer.toString(db.getCount());
    }
    
    @GET
    @Path("{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getRequest(@PathParam("id") Integer id){
        return db.getRequest(id);
    }
    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id")Integer id){
        db.removeRequest(id);
    }
}
