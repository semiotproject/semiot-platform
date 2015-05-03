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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.services.analyzing.cqels.Engine;
import ru.semiot.services.analyzing.wamp.Launcher;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
@Stateless
@Path("rest")
public class REST {

    private static final Logger logger = LoggerFactory
            .getLogger(REST.class);
    @Inject
    private DataBase db;

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void create(String request) {
        logger.info("Appending select");
        Engine.beforeClass();
        db.appendRequest(request);
        Engine.registerSelect(request);
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
    public String getRequest(@PathParam("id") Integer id) {
        return db.getRequest(id);
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String remove(@PathParam("id") Integer id) {
        return db.removeRequest(id);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String startLauncher() {
        logger.info("Starting WAMP connection");
        Engine.beforeClass();
        new Thread(new Runnable() {

            @Override
            public void run() {
                new Launcher().run();
            }
        }).start();
        return "WAMP connection created";
    }
}
