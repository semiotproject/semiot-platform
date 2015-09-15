package ru.semiot.platform.deviceproxyservice.manager;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;

public class WAMPClient implements Closeable, AutoCloseable {

	private static final WAMPClient INSTANCE = new WAMPClient();
	private WampClient client;

	private WAMPClient() {
	}

	public static WAMPClient getInstance() {
		return INSTANCE;
	}

	public Observable<WampClient.Status> init(String wampUri, String wampRealm, int wampReconnectInterval) throws ApplicationError {
		WampClientBuilder builder = new WampClientBuilder();
		builder.withUri(wampUri)
				.withRealm(wampRealm)
				.withInfiniteReconnects()
				.withReconnectInterval(wampReconnectInterval,
						TimeUnit.SECONDS);
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

	public void close() throws IOException {
		client.close();
	}

}