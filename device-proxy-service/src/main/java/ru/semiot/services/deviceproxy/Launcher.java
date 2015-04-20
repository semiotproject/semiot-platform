package ru.semiot.services.deviceproxy;

import java.io.IOException;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.semiot.services.deviceproxy.handlers.wamp.NewDeviceHandler;
import ws.wamp.jawampa.WampClient;

public class Launcher {

	private static final Logger logger = LoggerFactory
			.getLogger(Launcher.class);
	private static final ServiceConfig config = ConfigFactory
			.create(ServiceConfig.class);

	public static final void main(String[] args) {
		Launcher launcher = new Launcher();
		launcher.run();
	}

	private void run() {
		try (CoAPInterface coap = new CoAPInterface()) {
			coap.start();

			WAMPClient
					.getInstance()
					.init()
					.subscribe(
							(WampClient.Status newStatus) -> {
								if (newStatus == WampClient.Status.Connected) {
									logger.info("Connected to {}",
											config.wampUri());

									WAMPClient
											.getInstance()
											.subscribe(config.topicsNewDevice())
											.subscribe(new NewDeviceHandler());
								} else if (newStatus == WampClient.Status.Disconnected) {
									logger.info("Disconnected from {}",
											config.wampUri());
								} else if (newStatus == WampClient.Status.Connecting) {
									logger.debug("Connecting to {}",
											config.wampUri());
								}
							});

			synchronized (this) {
				while (!Thread.interrupted()) {
					logger.info("Press Ctrl+C to stop");
					wait();
				}
			}
		} catch (Exception ex) {
			logger.info(ex.getMessage(), ex);
		} finally {
			try {
				WAMPClient.getInstance().close();
			} catch (IOException ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	public static ServiceConfig getConfig() {
		return config;
	}

}
