package ru.semiot.services.analyzing.wamp;

import static ru.semiot.services.analyzing.ServiceConfig.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.auth.client.Ticket;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import ws.wamp.jawampa.SubscriptionFlags;

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

  public void init() throws Exception {
    WampClientBuilder builder = new WampClientBuilder();
    builder.withConnectorProvider(new NettyWampClientConnectorProvider())
        .withAuthId(config.wampUsername())
        .withAuthMethod(new Ticket(config.wampPassword()))
        .withUri(config.wampUri())
        .withRealm(config.wampRealm())
        .withInfiniteReconnects()
        .withReconnectInterval(config.wampReconnectInterval(),
            TimeUnit.SECONDS);
    client = builder.build();
    client.open();
    client.statusChanged().subscribe((WampClient.State newStatus) -> {
      if (newStatus instanceof WampClient.ConnectedState) {
        logger.info("Connected to {}",
            config.wampUri());
      } else if (newStatus instanceof WampClient.DisconnectedState) {
        logger.info("Disconnected from {}",
            config.wampUri());
      } else if (newStatus instanceof WampClient.ConnectingState) {
        logger.debug("Connecting to {}",
            config.wampUri());
      }
    });
  }

  public Observable<Long> publish(String topic, String message) {
    return client.publish(topic, message);
  }

  public Observable<String> subscribe(String topic) {
    logger.info("Made subscription to " + topic);

    return client.makeSubscription(topic, SubscriptionFlags.Prefix, String.class);
  }

  @Override
  public void close() throws IOException {
    client.close();
  }

}
