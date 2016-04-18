package ru.semiot.services.tsdbservice;

import com.datastax.driver.core.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static ru.semiot.services.tsdbservice.ServiceConfig.CONFIG;
import ru.semiot.services.tsdbservice.model.Observation;
import rx.Observable;
import rx.Subscriber;

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
                    "for_parameter text, value text" +
                    ");";
    private static final String CREATE_COMMAND_PROPERTY_TYPE =
            "CREATE TYPE IF NOT EXISTS semiot.command_property (" +
                    "property text," +
                    "value text," +
                    "datatype text" +
                    ");";
    private static final String CREATE_ACTUATION_TABLE =
            "CREATE TABLE IF NOT EXISTS semiot.actuation (" +
                    "  system_id text," +
                    "  event_time timestamp," +
                    "  command_type text," +
                    "  command_properties list<frozen <command_property>>," +
                    "  command_parameters list<frozen <command_parameter>>," +
                    "  PRIMARY KEY(system_id, event_time)" +
                    ")" +
                    "WITH CLUSTERING ORDER BY (event_time DESC);";

    private static volatile TSDBClient instance = null;

    Cluster cluster; 
    Session session;

    private TSDBClient() {

    }

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
        session.execute(CREATE_ACTUATION_TABLE);
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

}