package ru.semiot.services.analyzing.rest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.services.analyzing.cep.Engine;
import ru.semiot.services.analyzing.database.EventsDataBase;
import ru.semiot.services.analyzing.database.QueryDataBase;

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
    private QueryDataBase db;
    @Inject
    Engine engine;
    @Inject
    EventsDataBase dbe;

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
            if (!object.has("name") || !object.has("sparql") || !object.has("text")) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            JSONObject ret = db.appendQuery(object.getString("text"), object.getString("name"), object.getString("sparql"));
            int query_id = ret.getInt("id");
            if (!engine.registerQuery(query_id)) {
                db.removeQuery(query_id);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            logger.info("Query " + ret.getString("name") + " appended");
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
        /*JSONArray events = dbe.getEventsByQueryId(id);
        if (events != null) {
            ret.append("events", events);
        }*/
        return Response.ok(ret.toString()).build();
    }
        
    @GET
    @Path("{id}/events")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventsByTime(@PathParam("id") Integer id, 
            @DefaultValue("-1") @QueryParam("from") long start_timestamp, 
            @DefaultValue("-1") @QueryParam("to") long end_timestamp) {
        logger.debug("Return events");
        JSONArray events;
        if(start_timestamp!=-1 && end_timestamp!=-1)
            events = dbe.getEventsByTime(start_timestamp, end_timestamp, id);
        else
            events = dbe.getEventsByQueryId(id);
        if (events == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(events.toString()).build();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response remove(@PathParam("id") Integer id) {
        logger.debug("Removing query");
        engine.removeQuery(id);
        JSONObject ret = db.removeQuery(id);
        if (ret == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        logger.info("Query " + ret.getString("name") + " removed");
        return Response.ok().build();
    }
}
