package rs.proxy.service;

import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Date;

@Path("/commands")
public class CommandAPI {

    private DeviceManager manager;

    @GET
    public Response execute() {
        boolean ok = manager.registerCommand("{test: command blah blah}");

        return Response.ok(ok).build();
    }
}
