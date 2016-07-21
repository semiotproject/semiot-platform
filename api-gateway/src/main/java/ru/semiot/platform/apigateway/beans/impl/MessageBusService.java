package ru.semiot.platform.apigateway.beans.impl;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.apigateway.ServerConfig;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;

@Singleton
public class MessageBusService {

  private static final Logger logger
      = LoggerFactory.getLogger(MessageBusService.class);
  private static final ServerConfig config = ConfigFactory.create(ServerConfig.class);
  private final BehaviorSubject<WampClient> subject;
  private WampClient client;

  public MessageBusService() {
    this.subject = BehaviorSubject.create();
  }

  @PostConstruct
  public void init() {
    logger.info("initializing");
    final IWampConnectorProvider provider = new NettyWampClientConnectorProvider();

    try {
      WampClientBuilder builder = new WampClientBuilder();
      builder
          .withConnectorProvider(provider)
          .withUri(config.wampUri())
          .withRealm(config.wampRealm())
          .withInfiniteReconnects()
          .withReconnectInterval(5, TimeUnit.SECONDS);
      client = builder.build();

      client.statusChanged().subscribe((WampClient.State state) -> {
        if (state instanceof WampClient.ConnectedState) {
          logger.info("connected to WAMP router [{}]", client.routerUri().toASCIIString());

          subject.onNext(client);
        } else if (state instanceof WampClient.ConnectingState) {
          logger.info("connecting to WAMP router [{}]", client.routerUri().toASCIIString());

        } else if (state instanceof WampClient.DisconnectedState) {
          logger.info("disconnected from WAMP router [{}]", client.routerUri().toASCIIString());
        }
      });

      client.open();
    } catch (Exception ex) {
      logger.warn(ex.getMessage(), ex);
    }
  }

  public Observable<String> subscribe(String topic) {
    return Observable.create(o -> {
      subject.subscribe((c) -> {
        c.makeSubscription(topic, String.class).subscribe((m) -> {
          o.onNext(m);
        }, (e) -> {
          o.onError(e);
        });
      });
    });
  }

  @PreDestroy
  public void destroy() {
    logger.info("destroying");

    client.close().subscribe((c) -> {
      logger.info("WampClient closed!");
    });
  }

}
