package ru.semiot.services.tsdbservice.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.json.JSONObject;
import static ru.semiot.services.tsdbservice.ServiceConfig.CONFIG;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
@Path("/settings")
public class Settings {
        
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void setSettings(String str){
        JSONObject json = new JSONObject(str);
        String pass = json.optString("services.wamp.password");
        CONFIG.setProperty("services.wamp.password", pass);
        if(json.has("services.wamp.login")){
            CONFIG.setProperty("services.wamp.login", pass);
        }
    }
}
