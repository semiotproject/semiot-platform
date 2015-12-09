package ru.semiot.platform.drivers.dht22;

import java.io.Closeable;
import java.io.IOException;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;

public class CoAPInterface extends CoapServer
        implements Closeable, AutoCloseable {
    
    private static Endpoint endpoint; 

    public CoAPInterface() {
        super();
        endpoint = new CoAPEndpoint();
        addEndpoint(endpoint);
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
