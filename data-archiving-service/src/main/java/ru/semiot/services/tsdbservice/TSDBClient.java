package ru.semiot.services.tsdbservice;

import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;

import ru.semiot.services.tsdbservice.model.Observation;
import rx.Observable;

public class TSDBClient {

    private static final Logger logger = LoggerFactory
            .getLogger(TSDBClient.class);
    private static final ServiceConfig config = ConfigFactory
            .create(ServiceConfig.class);
    private static final String queryCreateKeyspace = "CREATE KEYSPACE IF NOT EXISTS semiot WITH replication = { "
            + "  'class': 'SimpleStrategy', " + "  'replication_factor': 1 } ;";

    private static final String queryCreateObservationTable = "CREATE TABLE IF NOT EXISTS semiot.observation ("
            + "  sensor_id text," + "  system_id text,"
            + "  event_time timestamp," + "  property text,"
            + "  feature_of_interest text," + "  value text,"
            + "  PRIMARY KEY ((system_id,sensor_id),event_time)" + ")"
            + "WITH CLUSTERING ORDER BY (event_time DESC);";

    private static volatile TSDBClient instance = null;

    Cluster cluster;
    Session session;

    private TSDBClient() {

    }

    private void createTables() {
        session.execute(queryCreateKeyspace);
        session.execute(queryCreateObservationTable);
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

    public void init() {
        logger.info("Connecting to {}", config.tsdbUrl());

        cluster = Cluster.builder().addContactPoint(config.tsdbUrl()).build(); // demo.semiot.ru
        session = cluster.connect();
        createTables();

        logger.info("Connected to {}", config.tsdbUrl());
    }

    public void stop() {
        cluster.close();
    }

    public void write(Observation obs) {
        session.execute(obs.insert());
    }

    public void deleteSystems(List<String> systemsId) {
        /*
         * for() {
         * 
         * }
         */
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
