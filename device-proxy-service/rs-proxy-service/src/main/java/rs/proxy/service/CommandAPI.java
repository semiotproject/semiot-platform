package rs.proxy.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriverManager;
import ru.semiot.platform.deviceproxyservice.api.drivers.CommandExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/commands")
public class CommandAPI {

    private static final Logger logger = LoggerFactory.getLogger(CommandAPI.class);

    private volatile DeviceDriverManager manager;

    @POST
    @Consumes("text/turtle")
    public Response execute(@QueryParam("system_id") String systemId, String command) {
        logger.debug("Command for system [{}]: {}", systemId, command);
        try {
            if (StringUtils.isEmpty(systemId) || StringUtils.isEmpty(command)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            manager.executeCommand(systemId, command);

            return Response.ok().build();
        } catch (CommandExecutionException cex) {
            if (cex.isNotFound()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            } else if (cex.isBadCommand()) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            } else {
                logger.warn(cex.getMessage(), cex);

                return Response.serverError().build();
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);

            return Response.serverError().build();
        }
    }

}
