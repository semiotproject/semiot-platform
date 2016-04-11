package ru.semiot.services.tsdbservice;

import java.io.IOException;
import java.net.URI;

import org.aeonbits.owner.ConfigFactory;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.semiot.services.tsdbservice.rest.ApplicationConfig;
import ru.semiot.services.tsdbservice.wamp.NewDeviceListener;
import ru.semiot.services.tsdbservice.wamp.WAMPClient;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;

import javax.ws.rs.core.UriBuilder;

public class Launcher {
    private static final Logger logger = LoggerFactory
            .getLogger(Launcher.class);
    private static final ServiceConfig config = ConfigFactory
            .create(ServiceConfig.class);

    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        launcher.run();
    }

    private void run() {
        try {
            WAMPClient
                    .getInstance()
                    .init()
                    .subscribe(
                            (WampClient.Status newStatus) -> {
                                if (newStatus == WampClient.Status.Connected) {
                                    logger.info("Connected to {}", config.wampUri());

                                    WAMPClient.getInstance().subscribe(
                                            config.topicsSubscriber())
                                            .subscribe(new NewDeviceListener());

                                } else if (newStatus == WampClient.Status.Disconnected) {
                                    logger.info("Disconnected from {}",
                                            config.wampUri());
                                } else if (newStatus == WampClient.Status.Connecting) {
                                    logger.debug("Connecting to {}",
                                            config.wampUri());
                                }
                            });

            TSDBClient.getInstance().init();

            URI uri = UriBuilder.fromUri("http://0.0.0.0/").port(8787).build();
            ResourceConfig resourceConfig = new ApplicationConfig();

            Server jettyServer = JettyHttpContainerFactory
                    .createServer(uri, resourceConfig);

            try {
                jettyServer.start();
                jettyServer.join();
            } finally {
                jettyServer.destroy();
            }
        } catch (ApplicationError | InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
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
}
