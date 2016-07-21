package ru.semiot.platform.apigateway.rest.config;

import static ru.semiot.commons.restapi.AsyncResponseHelper.resume;

import ru.semiot.commons.restapi.MediaType;
import ru.semiot.platform.apigateway.beans.impl.OSGiApiService;
import ru.semiot.platform.apigateway.config.BundleConstants;

import java.io.StringWriter;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;

@Path("/config/system")
@Stateless
public class SystemSettingsResource {

  @Inject
  private OSGiApiService service;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public void get(@Suspended AsyncResponse response) {
    service.managerIsConfigurated().map((Boolean isConfigured) -> {
      if (isConfigured.booleanValue()) {
        JsonObject props = service.getConfiguration(BundleConstants.MANAGER_PID)
            .toBlocking().first();

        StringWriter writer = new StringWriter();
        Json.createWriter(writer).writeObject(props);

        return writer.toString();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    }).subscribe(resume(response));
  }

}
