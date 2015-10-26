package ru.semiot.services.analyzing.wamp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.PostConstruct;
import javax.ejb.Stateful;
import javax.faces.bean.ManagedBean;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.services.analyzing.ServiceConfig;
import static ru.semiot.services.analyzing.ServiceConfig.config;
import rx.functions.Action1;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;

@Stateful
@ManagedBean(eager = true)
@javax.faces.bean.ApplicationScoped
public class Launcher {

    private static final Logger logger = LoggerFactory
            .getLogger(Launcher.class);

    @PostConstruct
    public void run() {
        logger.info("Start WAMP");
        connectWithDataStore();
        new Thread(new Runnable() {

            @Override
            public void run() {
                startWamp();
            }
        }).start();
    }

    private void connectWithDataStore() {
        try {
            URI uri = new URI(ServiceConfig.config.storeUrl());
            String URL = "http://" + uri.getHost() + ":" + uri.getPort();
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(URL);
            HttpResponse resp = null;

            while (true) {
                try {
                    resp = client.execute(request);
                    if (resp.getStatusLine().getStatusCode() == 200) {
                        logger.info("Connected to " + URL);
                        break;
                    }
                } catch (HttpHostConnectException ex) {
                    logger.info("Try to connect with " + URL);
                } catch (IOException ex) {
                    logger.error("Something went wrong with error:\n"+ex.getMessage());
                }
            }
        } catch (URISyntaxException ex) {
            logger.error("The storeURL is WRONG!!!");
        }
    }

    private void startWamp() {
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
