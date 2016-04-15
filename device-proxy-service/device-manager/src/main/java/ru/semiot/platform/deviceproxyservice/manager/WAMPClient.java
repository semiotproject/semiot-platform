package ru.semiot.platform.deviceproxyservice.manager;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.auth.client.Ticket;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

public class WAMPClient implements Closeable, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(WAMPClient.class);
    private static final WAMPClient INSTANCE = new WAMPClient();
    private WampClient client;

    private WAMPClient() {
    }

    public static WAMPClient getInstance() {
        return INSTANCE;
    }

    public static Subscriber onError() {
        return new Subscriber() {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable e) {
                logger.error(e.getMessage(), e);
            }

            @Override
            public void onNext(Object o) {}
        };
    }

    public Observable<WampClient.State> init(
            String wampUri, String wampRealm, int wampReconnectInterval,
            String authId, String ticket) 
            throws Exception  {
        WampClientBuilder builder = new WampClientBuilder();
        builder.withConnectorProvider(new NettyWampClientConnectorProvider())
                .withUri(wampUri)
                .withRealm(wampRealm)
                .withInfiniteReconnects()
                .withReconnectInterval(wampReconnectInterval,
                        TimeUnit.SECONDS)
                .withAuthId(authId)
                .withAuthMethod(new Ticket(ticket));
        client = builder.build();
        client.open();
        return client.statusChanged();
    }

    public Observable<Long> publish(String topic, String message) {
        return client.publish(topic, message);
    }

    public Observable<String> subscribe(String topic) {
        return client.makeSubscription(topic, String.class);
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

}