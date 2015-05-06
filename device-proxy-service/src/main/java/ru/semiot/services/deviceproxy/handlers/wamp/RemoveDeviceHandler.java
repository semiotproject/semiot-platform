package ru.semiot.services.deviceproxy.handlers.wamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.semiot.services.deviceproxy.Launcher;
import ru.semiot.services.deviceproxy.WAMPClient;
import rx.Observer;

public class RemoveDeviceHandler implements Observer<String> {
	private static final Logger logger = LoggerFactory
			.getLogger(RemoveDeviceHandler.class);

	public RemoveDeviceHandler() {

	}

	@Override
	public void onCompleted() {
		logger.debug("completed");
	}

	@Override
	public void onError(Throwable e) {
		logger.warn(e.getMessage(), e);
	}

	@Override
	public void onNext(final String message) {
		DeviceHandler.getInstance().removeDevice(message);
		WAMPClient.getInstance().publish(
				Launcher.getConfig().topicsRemoveDevice(), message);
	}
}
