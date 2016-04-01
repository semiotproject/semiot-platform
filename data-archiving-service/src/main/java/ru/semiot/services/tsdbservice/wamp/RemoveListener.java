package ru.semiot.services.tsdbservice.wamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observer;

public class RemoveListener implements Observer<String> {

	private static final Logger logger = LoggerFactory
			.getLogger(RemoveListener.class);

	@Override
	public void onCompleted() {

	}

	@Override
	public void onError(Throwable e) {
		logger.warn(e.getMessage(), e);
	}

	@Override
	public void onNext(String t) {
		// получаю массив topics
		/*
		 * for (String topic : topics) {
		 * WAMPClient.getInstance().unsubscribe(topic); // удаление метрики }
		 */

	}

}
