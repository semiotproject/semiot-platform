package ru.semiot.services.analyzing.rest;

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
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.services.analyzing.ServiceConfig;
import ru.semiot.services.analyzing.cqels.Engine;
import ru.semiot.services.analyzing.database.DataBase;
import ru.semiot.services.analyzing.wamp.Launcher;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
@Stateless
@Path("/")
public class Queries {

    private static final Logger logger = LoggerFactory
            .getLogger(Queries.class);
    
    @Inject
    private DataBase db;
    
    @GET
    @Path("qs")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getQueries(){
        logger.debug("Return queries");
        String [] ret = db.getQueries();
        if(ret == null)
            return Response.status(Response.Status.NOT_FOUND).build();        
        return Response.ok().type(MediaType.WILDCARD_TYPE).entity(ret).build();
    }
    
    @DELETE
    @Path("qs")
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeAllQueries(){
        logger.debug("Removing all queries");
        if(!db.removeQueries())
            return Response.status(Response.Status.FORBIDDEN).build();
        Engine.removeAllSelects();
        logger.info("All queries removed");
        return Response.ok().build();
    }
    
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response create(String request) {
        logger.debug("Appending select");
        db.appendQuery(request);
        Engine.beforeClass();
        Engine.registerSelect(request);
        logger.info("Select appended");
        return Response.ok().build();
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String count() {
        return Integer.toString(db.getCount());
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getQuery(@PathParam("id") Integer id) {
        logger.debug("Return query");
        String ret = db.getQuery(id);
        if(ret == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(ret).build();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response remove(@PathParam("id") Integer id) {
        logger.debug("Removing select");
        String ret = db.removeQuery(id);
        if(ret == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        Engine.removeSelect(ret);
        logger.info("Select removed");
        return Response.status(Response.Status.OK).entity(ret).build();
    }
    
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response startLauncher() {
        logger.info("Starting WAMP connection");
        Engine.beforeClass();
        if(ServiceConfig.config.isAutoLoaded() && db.getQueries()!=null)
            for(String s : db.getQueries())
                Engine.registerSelect(s);            
        new Thread(new Runnable() {

            @Override
            public void run() {
                new Launcher().run();
            }
        }).start();
        return Response.ok().entity("WAMP connection started").build();
    }
}
