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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.services.analyzing.cqels.Engine;
import ru.semiot.services.analyzing.database.DataBase;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
@Stateless
@Path("query")
public class Queries {

    private static final Logger logger = LoggerFactory
            .getLogger(Queries.class);

    @Inject
    private DataBase db;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQueries() {
        logger.debug("Return queries");
        JSONArray ret = db.getQueries();
        if (ret == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().entity(ret.toString()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(String request) {
        logger.debug("Appending query");
        if (request == null || request.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            JSONObject object = new JSONObject(request);

            if (!object.has("name") || !object.has("text")) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if (!Engine.getInstance().registerQuery(object.getString("text"))) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            JSONObject ret = db.appendQuery(object.getString("text"), object.getString("name"));
            logger.info("Query appended");
            return Response.ok(ret.toString()).build();
        } catch (JSONException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String count() {
        return Long.toString(db.getCount());
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQuery(@PathParam("id") Integer id) {
        logger.debug("Return query");
        JSONObject ret = db.getQuery(id);
        if (ret == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(ret.toString()).build();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response remove(@PathParam("id") Integer id) {
        logger.debug("Removing query");
        JSONObject ret = db.removeQuery(id);
        if (ret == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Engine.getInstance().removeQuery(ret.getString("text"));
        logger.info("Query removed");
        return Response.ok().build();
    }
}
