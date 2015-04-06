package ru.semiot.services.deviceproxy;

import java.io.Closeable;
import java.io.IOException;
import org.aeonbits.owner.ConfigFactory;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import ru.semiot.services.deviceproxy.resources.RegisterResource;

public class CoAPInterface extends CoapServer
        implements Closeable, AutoCloseable {
    
    private static final ServiceConfig config = 
            ConfigFactory.create(ServiceConfig.class);
    private static final Endpoint endpoint = new CoAPEndpoint(config.port());

    public CoAPInterface() {
        super();
        addEndpoint(endpoint);
        
        add(new RegisterResource());
    }
    
    public static Endpoint getEndpoint() {
        return endpoint;
    }
    
    @Override
    public void start() {
        super.start();
    }

    @Override
    public void close() throws IOException {
        super.destroy();
    }

}
