package ru.semiot.services.tsdbservice.rest;

import static ru.semiot.services.tsdbservice.ServiceConfig.CONFIG;

import org.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
@Path("/settings")
public class Settings {
  private static final String DOMAIN = "ru.semiot.platform.domain";
  private static final String WAMP_PASS = "ru.semiot.platform.wamp_password";
  private static final String WAMP_LOGIN = "ru.semiot.platform.wamp_login";

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public void setSettings(String str) {
    JSONObject json = new JSONObject(str);
    CONFIG.setProperty(WAMP_LOGIN, json.optString(WAMP_LOGIN));
    String prefix = json.optString(DOMAIN);
    if (!prefix.endsWith("/"))
      prefix += "/";
    CONFIG.setProperty(DOMAIN, prefix);
    CONFIG.setProperty(WAMP_PASS, json.optString(WAMP_PASS));
  }
}
