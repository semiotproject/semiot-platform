package rs.proxy.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriverManager;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Path("/commands")
public class CommandAPI {

    private static final Logger logger = LoggerFactory.getLogger(CommandAPI.class);

    private volatile DeviceDriverManager manager;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response execute(@QueryParam("system_id") String systemId, String command) {
        try {
            if (StringUtils.isEmpty(systemId) || StringUtils.isEmpty(command)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            logger.info(command);

            manager.executeCommand(systemId, command);

            return Response.ok().build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
}
