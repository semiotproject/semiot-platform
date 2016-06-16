package ru.semiot.services.tsdbservice;

import static ru.semiot.services.tsdbservice.ServiceConfig.CONFIG;

import org.aeonbits.owner.event.RollbackBatchException;
import org.aeonbits.owner.event.RollbackOperationException;
import org.aeonbits.owner.event.TransactionalPropertyChangeListener;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.services.tsdbservice.rest.ApplicationConfig;
import ru.semiot.services.tsdbservice.wamp.NewDeviceListener;
import ru.semiot.services.tsdbservice.wamp.WAMPClient;
import ws.wamp.jawampa.WampClient;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

public class Launcher {

  private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

  private static final long TIMEOUT = 5000;

  public static void main(String[] args) {
    Launcher launcher = new Launcher();
    launcher.run();
  }

  private void run() {
    boolean isConnected = false;
    while (!isConnected) {
      try {
        TSDBClient.getInstance().init();
        logger.info("Connected to the tsdb");
        isConnected = true;
      } catch (Exception e) {
        logger.warn(e.getMessage());
        logger.warn("Can't connect to the tsdb! Retry in {}ms", TIMEOUT);
        try {
          Thread.sleep(TIMEOUT);
        } catch (InterruptedException ex) {
          logger.error(ex.getMessage());
        }
      }
    }
    try {

      CONFIG.addPropertyChangeListener("ru.semiot.platform.wamp_password",
          new TransactionalPropertyChangeListenerImpl());

      URI uri = UriBuilder.fromUri("http://0.0.0.0/").port(8787).build();
      ResourceConfig resourceConfig = new ApplicationConfig();

      Server jettyServer = JettyHttpContainerFactory.createServer(uri, resourceConfig);

      try {
        jettyServer.start();
        jettyServer.join();
      } finally {
        jettyServer.destroy();
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    } finally {
      try {
        TSDBClient.getInstance().stop();
        WAMPClient.getInstance().close();
      } catch (IOException ex) {
        logger.error(ex.getMessage(), ex);
      }
    }
  }

  private class TransactionalPropertyChangeListenerImpl
      implements TransactionalPropertyChangeListener {

    @Override
    public void beforePropertyChange(PropertyChangeEvent event)
        throws RollbackOperationException, RollbackBatchException {}

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      try {
        WAMPClient.getInstance().init().subscribe((WampClient.State newState) -> {
          if (newState instanceof WampClient.ConnectedState) {
            logger.info("Connected to {}", CONFIG.wampUri());

            try {
              NewDeviceListener newDeviceListener = new NewDeviceListener();
              WAMPClient.getInstance()
                  .subscribe(CONFIG.topicsSubscriber())
                  .subscribe(newDeviceListener);

              newDeviceListener.loadDeviceTopicsAndSubscribe();
            } catch (Throwable ex) {
              logger.debug(ex.getMessage(), ex);
            }

          } else if (newState instanceof WampClient.DisconnectedState) {
            logger.info("Disconnected from {}", CONFIG.wampUri());
          } else if (newState instanceof WampClient.ConnectingState) {
            logger.debug("Connecting to {}", CONFIG.wampUri());
          }
        });
      } catch (Throwable ex) {
        logger.error(ex.getMessage(), ex);
      }
    }
  }
}
