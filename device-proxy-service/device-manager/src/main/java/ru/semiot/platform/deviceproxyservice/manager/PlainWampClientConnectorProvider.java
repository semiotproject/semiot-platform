package ru.semiot.platform.deviceproxyservice.manager;


import io.netty.channel.nio.NioEventLoopGroup;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class PlainWampClientConnectorProvider extends NettyWampClientConnectorProvider {

  @Override
  public ScheduledExecutorService createScheduler() {
    NioEventLoopGroup scheduler = new NioEventLoopGroup(10, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r, "WampClientEventLoop");
        t.setDaemon(true);
        return t;
      }
    });
    return scheduler;
  }
}
