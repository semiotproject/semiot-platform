package ru.semiot.services.analyzing.wamp;

import ru.semiot.services.analyzing.ServiceConfig;
import java.io.IOException;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;

public class Launcher {

    private static final Logger logger = LoggerFactory
            .getLogger(Launcher.class);
    private static final ServiceConfig config = ConfigFactory
            .create(ServiceConfig.class);

    public void run() {
        try {
            WAMPClient
                    .getInstance()
                    .init()
                    .subscribe(new Action1<WampClient.Status>() {

                public void call(WampClient.Status newStatus) {
                    if (newStatus == WampClient.Status.Connected) {
                        logger.info("Connected to {}",
                                config.wampUri());
                        
                        WAMPClient
                                .getInstance()
                                .subscribe(
                                        config.topicsSubscriber())
                                .subscribe(new SubscribeListener());
                    } else if (newStatus == WampClient.Status.Disconnected) {
                        logger.info("Disconnected from {}",
                                config.wampUri());
                    } else if (newStatus == WampClient.Status.Connecting) {
                        logger.debug("Connecting to {}",
                                config.wampUri());
                    }
                }
            });

            synchronized (this) {
                while (!Thread.interrupted()) {
                    wait();
                }
            }
        } catch (ApplicationError | InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            try {
                WAMPClient.getInstance().close();
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
}
