package ru.semiot.services.deviceproxy;

import java.io.Closeable;
import java.io.IOException;
import org.aeonbits.owner.ConfigFactory;
import org.eclipse.californium.core.CoapServer;
import ru.semiot.services.deviceproxy.resources.RegisterResource;

public class CoAPInterface extends CoapServer
        implements Closeable, AutoCloseable {
    
    private static final ServiceConfig config = 
            ConfigFactory.create(ServiceConfig.class);

    public CoAPInterface() {
        super(config.port());
        
        add(new RegisterResource());
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
