package ru.semiot.platform.drivers.simulator;

import java.io.Closeable;
import java.io.IOException;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import ru.semiot.platform.drivers.simulator.resources.RegisterResource;

public class CoAPInterface extends CoapServer
        implements Closeable, AutoCloseable {
    
    private static Endpoint endpoint; 

    public CoAPInterface(DeviceDriverImpl deviceDriverImpl) {
        super();
        endpoint = new CoAPEndpoint(deviceDriverImpl.getPort());
        addEndpoint(endpoint);
        
        add(new RegisterResource(deviceDriverImpl));
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
