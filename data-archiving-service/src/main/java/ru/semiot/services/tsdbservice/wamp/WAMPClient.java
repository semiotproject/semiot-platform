package ru.semiot.services.tsdbservice.wamp;

import static ru.semiot.services.tsdbservice.ServiceConfig.CONFIG;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;
import ws.wamp.jawampa.SubscriptionFlags;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.auth.client.Ticket;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class WAMPClient implements Closeable, AutoCloseable {

  private static final Logger logger = LoggerFactory
      .getLogger(WAMPClient.class);
  private static final WAMPClient INSTANCE = new WAMPClient();
  private final HashMap<String, Subscription> subscriptions = new HashMap<>();
  private WampClient client;

  private WAMPClient() {}

  public static WAMPClient getInstance() {
    return INSTANCE;
  }

  public Observable<WampClient.State> init() throws Exception {
    WampClientBuilder builder = new WampClientBuilder();
    builder.withUri(CONFIG.wampUri()).withRealm(CONFIG.wampRealm())
        .withInfiniteReconnects()
        .withReconnectInterval(CONFIG.wampReconnectInterval(), TimeUnit.SECONDS)
        .withConnectorProvider(new PlainWampClientConnectorProvider())
        .withAuthId(CONFIG.wampLogin())
        .withAuthMethod(new Ticket(CONFIG.wampPassword()));
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
  
  public Observable<String> subscribe(String topic, SubscriptionFlags flag) {
    logger.info("Subscribed to {} topic", topic);
    return client.makeSubscription(topic, flag, String.class);
  }

  @Override
  public void close() throws IOException {
    if (client != null) {
      client.close();
    }
  }

  public void addSubscription(String key, Subscription value) {
    subscriptions.put(key, value);
  }

  public void unsubscribe(String key) {
    Subscription subscription = subscriptions.get(key);
    if (subscription != null) {
      subscription.unsubscribe();
      subscriptions.remove(key);
    }
  }

}
