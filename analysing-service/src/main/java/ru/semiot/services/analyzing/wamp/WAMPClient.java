package ru.semiot.services.analyzing.wamp;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static ru.semiot.services.analyzing.ServiceConfig.config;
import rx.Observable;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

public class WAMPClient implements Closeable, AutoCloseable {

    private static final Logger logger = LoggerFactory
            .getLogger(WAMPClient.class);
    private static final WAMPClient INSTANCE = new WAMPClient();   
    private WampClient client;

    private WAMPClient() {
    }

    public static WAMPClient getInstance() {
        return INSTANCE;
    }

    public Observable<WampClient.State> init() throws Exception {
        WampClientBuilder builder = new WampClientBuilder();
        builder.withConnectorProvider(new NettyWampClientConnectorProvider())
                .withUri(config.wampUri())
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
        logger.info("Made subscription to " + topic);

        return client.makeSubscription(topic, String.class);
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

}
