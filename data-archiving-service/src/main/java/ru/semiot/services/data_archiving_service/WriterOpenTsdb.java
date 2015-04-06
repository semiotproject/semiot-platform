package ru.semiot.services.data_archiving_service;

import java.util.HashMap;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sps.metrics.opentsdb.OpenTsdb;
import com.github.sps.metrics.opentsdb.OpenTsdbMetric;

public class WriterOpenTsdb {

	private static final Logger logger = LoggerFactory
			.getLogger(WriterOpenTsdb.class);
	private static final ServiceConfig config = ConfigFactory
			.create(ServiceConfig.class);
	private static final WriterOpenTsdb INSTANCE = new WriterOpenTsdb();
	private final OpenTsdb open;

	private WriterOpenTsdb() {
		open = OpenTsdb.forService(config.tsdbUrl()).create();
	}

	public static WriterOpenTsdb getInstance() {
		return INSTANCE;
	}

	// TODO добавить множественную загрузку
	public void send(String nameMetric, Object value, Long timestamp,
			HashMap<String, String> tags) {
		open.send(OpenTsdbMetric.named(nameMetric).withValue(value)
				.withTimestamp(timestamp).withTags(tags).build());
		logger.info("Send metric=" + nameMetric + ", value=" + value
				+ ", timestamp=" + String.valueOf(timestamp));
	}

}
