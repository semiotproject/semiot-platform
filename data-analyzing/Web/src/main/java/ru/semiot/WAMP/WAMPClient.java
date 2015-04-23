package ru.semiot.WAMP;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static ru.semiot.WAMP.ServiceConfig.config;
import rx.Observable;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;

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

	public Observable<WampClient.Status> init() throws ApplicationError {
		WampClientBuilder builder = new WampClientBuilder();
		builder.withUri(config.wampUri())
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
