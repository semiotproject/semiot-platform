package ru.semiot.platform.apigateway.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.restapi.MediaType;
import ru.semiot.platform.apigateway.utils.Credentials;
import ru.semiot.platform.apigateway.utils.DataBase;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/user")
@Stateless
public class UserResource {

  private static final Logger logger = LoggerFactory.getLogger(UserResource.class);
  private static final String USER_INFORMATION =
      "{\"username\": \"${USERNAME}\", \"password\": \"${PASSWORD}\", \"role\": \"${ROLE}\"}";

  @Inject
  private DataBase db;

  @Context
  private UriInfo uriInfo;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public void getUser(@Context HttpServletRequest req, @Suspended AsyncResponse response) {
    Credentials credentials = db.getUser(req.getRemoteUser());
    if (credentials != null) {
      String message = USER_INFORMATION
          .replace("${USERNAME}", credentials.getLogin())
          .replace("${PASSWORD}", credentials.getPassword())
          .replace("${ROLE}", credentials.getRole());
      response.resume(Response.ok(message).build());
    } else {
      response.resume(Response.status(Response.Status.UNAUTHORIZED).build());
    }
  }

  @GET
  @Path("/logout")
  public Response logout(@Context HttpServletRequest req) {
    try {
      if (req.getSession(false) != null) {
        //Invalidates the current session
        req.getSession(false).invalidate();
      }

      req.logout();

      return Response.seeOther(uriInfo.getBaseUriBuilder().replacePath("/").build())
          .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store")
          .header(HttpHeaders.EXPIRES, new java.util.Date().toString())
          .build();
    } catch (ServletException e) {
      logger.error(e.getMessage(), e);
      return Response.serverError().build();
    }
  }
}
