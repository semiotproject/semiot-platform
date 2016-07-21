package ru.semiot.platform.apigateway.rest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.restapi.MediaType;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

public class AbstractSystemResource {

  private static final Logger logger = LoggerFactory.getLogger(AbstractSystemResource.class);
  private String systemsHtml;

  public AbstractSystemResource() {
    try {
      this.systemsHtml = IOUtils.toString(
          this.getClass().getClassLoader().getResource("systems.html"));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  @GET
  @Path("{systems_path:.*}")
  @Produces({MediaType.TEXT_HTML, MediaType.TEXT_PLAIN})
  public String redirectToExplorer() {
    return systemsHtml;
  }

  @GET
  @Produces({MediaType.TEXT_HTML, MediaType.TEXT_PLAIN})
  public String redirectToExplorerWild() {
    return redirectToExplorer();
  }

}
