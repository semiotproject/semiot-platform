package ru.semiot.platform.deviceproxyservice.rest;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.CommandExecutionException;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriverManager;

import java.io.StringReader;
import java.io.StringWriter;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/commands")
public class CommandAPI {

  private static final Logger logger = LoggerFactory.getLogger(CommandAPI.class);

  private volatile DeviceDriverManager manager;

  @POST
  @Produces("application/ld+json")
  @Consumes("text/turtle")
  public Response execute(@QueryParam("system_id") String systemId, String command) {
    logger.debug("Command for system [{}]: {}", systemId, command);
    try {
      if (StringUtils.isEmpty(systemId) || StringUtils.isEmpty(command)) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }

      StringReader reader = new StringReader(command);

      Model actuation = manager.executeCommand(systemId,
          ModelFactory.createDefaultModel()
              .read(reader, null, RDFLanguages.strLangTurtle));

      StringWriter writer = new StringWriter();
      actuation.write(writer, RDFLanguages.strLangJSONLD);

      return Response.ok(writer.toString(), "application/ld+json").build();
    } catch (CommandExecutionException cex) {
      logger.debug(cex.getMessage(), cex);

      if (cex.isNotFound()) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity(cex.getMessage()).build();
      } else if (cex.isBadCommand()) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(cex.getMessage()).build();
      } else {
        logger.warn(cex.getMessage(), cex);

        return Response.serverError().entity(cex.getMessage()).build();
      }
    } catch (Throwable e) {
      logger.error(e.getMessage(), e);

      return Response.serverError().build();
    }
  }

}
