package ru.semiot.wamp;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static ru.semiot.wamp.ServiceConfig.config;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
@Stateless
@Path("/")
public class REST {
    private static final Logger logger = LoggerFactory
            .getLogger(REST.class);
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String startLauncher() {
        logger.info("Get request. Create new launcher");
        new Thread(new Runnable() {

            @Override
            public void run() {
                new Launcher().run();
            }
        }).start();
        return "WAMP connection created";
    }

    @POST
    @Path("alert")
    @Consumes(MediaType.TEXT_PLAIN)
    public void alert(String al) {
        logger.info("Get alert!" + al);
        WAMPClient.getInstance().publish(config.topicsAlert(), al);
    }
}
