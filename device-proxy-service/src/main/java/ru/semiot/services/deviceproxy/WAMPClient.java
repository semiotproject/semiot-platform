package ru.semiot.services.deviceproxy;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;

public class WAMPClient implements Closeable, AutoCloseable {

    private static final Logger logger
            = LoggerFactory.getLogger(WAMPClient.class);
    private static final ServiceConfig config
            = ConfigFactory.create(ServiceConfig.class);
    private static final WAMPClient INSTANCE = new WAMPClient();
    private WampClient client;

    private WAMPClient() {
    }

    public static WAMPClient getInstance() {
        return INSTANCE;
    }

    public void init() throws ApplicationError {
        WampClientBuilder builder = new WampClientBuilder();
        builder.withUri(config.wampUri())
                .withRealm(config.wampRealm())
                .withInfiniteReconnects()
                .withReconnectInterval(
                        config.wampReconnectInterval(), TimeUnit.SECONDS);
        client = builder.build();
        client.statusChanged().subscribe((WampClient.Status newStatus) -> {
            if (newStatus == WampClient.Status.Connected) {
                logger.info("Connected to {}", config.wampUri());
            } else if (newStatus == WampClient.Status.Disconnected) {
                logger.info("Disconnected from {}", config.wampUri());
            } else if (newStatus == WampClient.Status.Connecting) {
                logger.debug("Connecting to {}", config.wampUri());
            }
        });
        client.open();
    }

    public Observable<Long> publish(String topic, String message) {
        return client.publish(topic, message);
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

}
