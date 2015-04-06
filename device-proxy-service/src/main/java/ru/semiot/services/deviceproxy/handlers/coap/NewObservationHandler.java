package ru.semiot.services.deviceproxy.handlers.coap;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.services.deviceproxy.WAMPClient;

public class NewObservationHandler implements CoapHandler {

    private static final Logger logger = LoggerFactory.getLogger(
            NewObservationHandler.class);
    private final String topic;
    private final WAMPClient wampClient = WAMPClient.getInstance();
    private CoapObserveRelation relation;

    public NewObservationHandler(final String topic) {
        this.topic = topic;
    }

    public void setRelation(final CoapObserveRelation relation) {
        this.relation = relation;
    }

    @Override
    public void onLoad(CoapResponse response) {
        if (response.getCode() == CoAP.ResponseCode.NOT_FOUND) {
            logger.warn("Path {} doesn't exist. Cancel subscription!", topic);
            relation.reactiveCancel();
        } else if (response.getCode() == CoAP.ResponseCode.CONTENT) {
            try {
                wampClient.publish(topic, response.getResponseText());
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        } else {
            logger.warn("Received unexpected response: {} {}",
                    response.getCode(), response.getResponseText());
        }
    }

    @Override
    public void onError() {
        logger.warn("Something went wrong! Stop proxying to {} topic", topic);
        relation.reactiveCancel();
    }

}
