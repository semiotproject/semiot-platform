package ru.semiot.services.tsdbservice;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static ru.semiot.services.tsdbservice.ServiceConfig.CONFIG;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.services.tsdbservice.model.Observation;
import rx.Observable;
import rx.Subscriber;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

public class TSDBClient {

  private static final Logger logger = LoggerFactory.getLogger(TSDBClient.class);
  private static final String CREATE_KEYSPACE =
      "CREATE KEYSPACE IF NOT EXISTS semiot WITH replication = { "
          + "  'class': 'SimpleStrategy', "
          + "  'replication_factor': 1 } ;";
  private static final String CREATE_OBSERVATION_TABLE =
      "CREATE TABLE IF NOT EXISTS semiot.observation ("
          + "  sensor_id text,"
          + "  system_id text,"
          + "  event_time timestamp,"
          + "  property text,"
          + "  feature_of_interest text,"
          + "  value text,"
          + "  PRIMARY KEY ((system_id,sensor_id),event_time)"
          + ")" +
          "WITH CLUSTERING ORDER BY (event_time DESC);";
  private static final String CREATE_COMMAND_PARAMETER_TYPE =
      "CREATE TYPE IF NOT EXISTS semiot.command_parameter (" +
          "for_parameter text, value text, datatype text" +
          ");";
  private static final String CREATE_COMMAND_PROPERTY_TYPE =
      "CREATE TYPE IF NOT EXISTS semiot.command_property (" +
          "property text," +
          "value text," +
          "datatype text" +
          ");";
  private static final String CREATE_COMMANDRESULT_TABLE =
      "CREATE TABLE IF NOT EXISTS semiot.commandresult (" +
          " system_id text," +
          " process_id text," +
          " event_time timestamp," +
          " command_type text," +
          " command_properties list<frozen <command_property>>," +
          " command_parameters list<frozen <command_parameter>>," +
          " PRIMARY KEY((system_id, process_id), event_time)" +
          ")" +
          "WITH CLUSTERING ORDER BY (event_time DESC);";
  private static final DateTimeFormatter CQL_TIMESTAMP_FORMAT = new DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .append(DateTimeFormatter.ISO_LOCAL_DATE)
      .appendLiteral('T')
      .appendValue(HOUR_OF_DAY, 2)
      .appendLiteral(':')
      .appendValue(MINUTE_OF_HOUR, 2)
      .optionalStart()
      .appendLiteral(':')
      .appendValue(SECOND_OF_MINUTE, 2)
      .optionalStart()
      .appendFraction(NANO_OF_SECOND, 2, 9, true)
      .appendOffsetId()
      .parseStrict()
      .toFormatter();

  private static volatile TSDBClient instance = null;

  Cluster cluster;
  Session session;

  private TSDBClient() {}

  public static TSDBClient getInstance() {
    if (instance == null) {
      synchronized (TSDBClient.class) {
        if (instance == null) {
          instance = new TSDBClient();
        }
      }
    }
    return instance;
  }

  public static Subscriber onError() {
    return new Subscriber() {
      @Override
      public void onCompleted() {}

      @Override
      public void onError(Throwable e) {
        logger.error(e.getMessage(), e);
      }

      @Override
      public void onNext(Object o) {}
    };
  }

  public void init() {
    logger.info("Connecting to {}", CONFIG.tsdbUrl());

    cluster = Cluster.builder().addContactPoint(CONFIG.tsdbUrl()).build();
    session = cluster.connect();

    createTables();

    logger.info("Connected to {}", CONFIG.tsdbUrl());
  }

  private void createTables() {
    session.execute(CREATE_KEYSPACE);
    logger.info("Created 'semiot' keyspace");

    session.execute(CREATE_OBSERVATION_TABLE);
    session.execute(CREATE_COMMAND_PARAMETER_TYPE);
    session.execute(CREATE_COMMAND_PROPERTY_TYPE);
    session.execute(CREATE_COMMANDRESULT_TABLE);
    logger.info("Created types and tables!");
  }

  public void stop() {
    cluster.close();
  }

  public void write(Observation obs) {
    session.execute(obs.insert());
  }

  public Observable<ResultSet> executeAsync(Statement statement) {
    ResultSetFuture rsf = session.executeAsync(statement);

    return Observable.from(rsf);
  }

  public Observable<ResultSet> executeAsync(String query) {
    ResultSetFuture rsf = session.executeAsync(query);

    return Observable.from(rsf);
  }

  public Observable<ResultSet> executeAsync(List<String> queries) {
    BatchStatement batch = new BatchStatement();
    for (String query : queries) {
      batch.add(new SimpleStatement(query));
    }
    ResultSetFuture rsf = session.executeAsync(batch);

    return Observable.from(rsf);
  }

  public static String formatToCQLTimestamp(ZonedDateTime dateTime) {
    return dateTime.format(CQL_TIMESTAMP_FORMAT);
  }
}
