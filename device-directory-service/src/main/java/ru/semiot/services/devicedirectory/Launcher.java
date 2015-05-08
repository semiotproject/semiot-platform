package ru.semiot.services.devicedirectory;

import java.io.IOException;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;

public class Launcher {

	private static final ServiceConfig config = ConfigFactory
			.create(ServiceConfig.class);
	private static final Logger logger = LoggerFactory
			.getLogger(Launcher.class);

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
									logger.info("Connected to {}",
											config.wampUri());

									WAMPClient.getInstance()
											.subscribe(config.topicsRegister())
											.subscribe(new RegisterListener());

									WAMPClient
											.getInstance()
											.subscribe(config.topicsInactive())
											.subscribe(
													new InactiveDeviceListener());

									WAMPClient
											.getInstance()
											.subscribe(
													config.topicsRemoveDevice())
											.subscribe(
													new RemoveDeviceListener());
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

	public static ServiceConfig getConfig() {
		return config;
	}
}
