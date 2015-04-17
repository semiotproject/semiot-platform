package ru.semiot.services.data_archiving_service;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
	private static volatile WriterOpenTsdb instance = null;
	private final ScheduledExecutorService scheduler;
	private final ScheduledRecord scheduledRecord;
	private ScheduledFuture handle = null;
	private final OpenTsdb open;
	private Set<OpenTsdbMetric> metrics = new HashSet<OpenTsdbMetric>();
	final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private WriterOpenTsdb() {
		logger.info("Connecting to {}", config.tsdbUrl());
		open = OpenTsdb.forService(config.tsdbUrl()).create();
		this.scheduler = Executors.newScheduledThreadPool(1);
		this.scheduledRecord = new ScheduledRecord();
		logger.info("Connected to {}", config.tsdbUrl());
	}

	public static WriterOpenTsdb getInstance() {
		if (instance == null)
			synchronized (WriterOpenTsdb.class) {
				if (instance == null)
					instance = new WriterOpenTsdb();
			}
		return instance;
	}

	/**
	 * Запись множества метрик
	 * 
	 * @param metriс
	 */
	public void send(Set<OpenTsdbMetric> metrics) {
		logger.info("Send metrics");
		open.send(metrics);
	}

	/**
	 * Добавление метрики в список для записи каждые 5 секунда
	 * 
	 * @param nameMetric
	 * @param value
	 * @param timestamp
	 * @param tags
	 */
	public void sendMetricToSet(String nameMetric, Object value,
			Long timestamp, Map<String, String> tags) {
		logger.info("Add to set metric=" + nameMetric + ", value=" + value
				+ ", timestamp=" + String.valueOf(timestamp));
		rwl.writeLock().lock(); // lock write
		try {
			metrics.add(OpenTsdbMetric.named(nameMetric).withValue(value)
					.withTimestamp(timestamp).withTags(tags).build());
		} finally {
			rwl.writeLock().unlock();// unlock write
		}
	}

	/**
	 * Запись одной метрики
	 * 
	 * @param nameMetric
	 * @param value
	 * @param timestamp
	 * @param tags
	 */
	public void send(String nameMetric, Object value, Long timestamp,
			Map<String, String> tags) {
		logger.info("Send metric=" + nameMetric + ", value=" + value
				+ ", timestamp=" + String.valueOf(timestamp));
		open.send(OpenTsdbMetric.named(nameMetric).withValue(value)
				.withTimestamp(timestamp).withTags(tags).build());
	}

	public void start() {
		if (this.handle != null)
			stop();

		int nDelay = 5;
		this.handle = this.scheduler.scheduleAtFixedRate(this.scheduledRecord,
				0, nDelay, SECONDS);
		logger.info("UScheduled started. Repeat will do every "
				+ String.valueOf(nDelay) + " seconds");
	}

	public void stop() {
		if (handle == null)
			return;

		handle.cancel(true);
		handle = null;
		logger.info("UScheduled stoped");
	}

	private class ScheduledRecord implements Runnable {
		@Override
		public void run() {
			logger.info("ScheduledRecording start");

			if (metrics != null && !metrics.isEmpty()) {
				rwl.readLock().lock(); // lock read
				try {
					send(metrics);
					metrics.clear();
				} finally {
					rwl.readLock().unlock(); // unlock read
				}
			}

			logger.info("ScheduledRecording complete");
		}
	}

}
