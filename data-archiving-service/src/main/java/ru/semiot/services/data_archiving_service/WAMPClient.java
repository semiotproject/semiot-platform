package ru.semiot.services.data_archiving_service;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;

public class WAMPClient implements Closeable, AutoCloseable {

    private static final Logger logger = LoggerFactory
            .getLogger(WAMPClient.class);
    private static final ServiceConfig config = ConfigFactory
            .create(ServiceConfig.class);
    private static final WAMPClient INSTANCE = new WAMPClient();
    private final HashMap<String, Subscription> sensorSubscriptions = new HashMap<>();
    private WampClient client;

    private WAMPClient() {
    }

    public static WAMPClient getInstance() {
        return INSTANCE;
    }

    public Observable<WampClient.State> init() throws Exception {
        WampClientBuilder builder = new WampClientBuilder();
        builder.withUri(config.wampUri())
                .withRealm(config.wampRealm())
                .withInfiniteReconnects()
                .withReconnectInterval(config.wampReconnectInterval(),
                        TimeUnit.SECONDS);
        client = builder.build();
        client.open();
        return client.statusChanged();
    }

    public Observable<Long> publish(String topic, String message) {
        return client.publish(topic, message);
    }

    public Observable<String> subscribe(String topic) {
        logger.info("Subscribed to {} topic", topic);
        return client.makeSubscription(topic, String.class);
    }

    @Override
    public void close() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    public void addSubscription(String key, Subscription value) {
        sensorSubscriptions.put(key, value);
    }

    public void unsubscribe(String key) {
        Subscription subscription = sensorSubscriptions.get(key);
        if (subscription != null) {
            subscription.unsubscribe();
            sensorSubscriptions.remove(key);
        }
    }

}
