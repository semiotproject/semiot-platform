package ru.semiot.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
import ru.semiot.cqels.Engine;

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
    public void create(String request){
        Engine.beforeClass();
        db.appendRequest(request);
        Engine.registerSelect(request);
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
    @Produces(MediaType.TEXT_PLAIN)
    public String remove(@PathParam("id")Integer id){
        return db.removeRequest(id);
    }
    @GET
    public void startLauncher() throws MalformedURLException, IOException{
        Engine.beforeClass();    
        HttpURLConnection conn = (HttpURLConnection)new URL ("http://localhost:8080/WAMP-1.0-SNAPSHOT/wamp/").openConnection();
        conn.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                                    conn.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) 
            logger.debug(inputLine);
        in.close();
    }
    @POST
    @Path("data")
    @Consumes(MediaType.TEXT_PLAIN)
    public void appendData(String data){ 
        Engine.appendData(data);
    }
}
