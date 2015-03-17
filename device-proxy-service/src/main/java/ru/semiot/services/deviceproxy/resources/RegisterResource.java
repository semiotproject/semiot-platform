package ru.semiot.services.deviceproxy.resources;

import org.aeonbits.owner.ConfigFactory;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.services.deviceproxy.ServiceConfig;
import ru.semiot.services.deviceproxy.WAMPClient;

public class RegisterResource extends CoapResource {

    private static final ServiceConfig config
            = ConfigFactory.create(ServiceConfig.class);
    private static final Logger logger = LoggerFactory.getLogger(
            RegisterResource.class);
    private final WAMPClient wampClient = WAMPClient.getInstance();

    public RegisterResource() {
        super("register");
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        final String description = exchange.getRequestText();
        wampClient.publish(config.topicsRegister(), description).subscribe(
                id -> {
                    exchange.respond(CoAP.ResponseCode.CREATED);
                }, (Throwable error) -> {
                    logger.warn(error.getMessage(), error);
                    exchange.respond(CoAP.ResponseCode.SERVICE_UNAVAILABLE);
                }
        );
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        super.handleDELETE(exchange);
    }

}
