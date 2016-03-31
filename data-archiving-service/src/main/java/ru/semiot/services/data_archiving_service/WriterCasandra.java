package ru.semiot.services.data_archiving_service;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class WriterCasandra {
	
	private static final Logger logger = LoggerFactory
			.getLogger(WriterCasandra.class);
	private static final ServiceConfig config = ConfigFactory
			.create(ServiceConfig.class);
	private static final String queryCreateKeyspace = 
			"CREATE KEYSPACE IF NOT EXISTS semiot WITH replication = { "
			+ "  'class': 'SimpleStrategy', "
			+ "  'replication_factor': 1 } ;";
	
	private static final String queryCreateObservationTable = 
			"CREATE TABLE IF NOT EXISTS semiot.observation ("
			+ "  sensor_id text,"
			+ "  system_id text,"
			+ "  event_time timestamp,"
			+ "  property text,"
			+ "  feature_of_property text,"
			+ "  observation_value text,"
			+ "  PRIMARY KEY ((system_id,sensor_id),event_time)"
			+ ");";
		
	private static volatile WriterCasandra instance = null;
	    
	Cluster cluster;
	Session session;
	
	private WriterCasandra() {
       
    }

    public static WriterCasandra getInstance() {
        if (instance == null) {
            synchronized (WriterCasandra.class) {
                if (instance == null) {
                    instance = new WriterCasandra();
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
    
    void stop() {
    	cluster.close();
    }
	
	void send(Observation obs) {
		session.execute(obs.insert());
	}
	
	void createTables() {
		// BatchStatement batch = new BatchStatement();
		// batch.add(new SimpleStatement(queryCreateKeyspace));
		// batch.add(new SimpleStatement(queryCreateObservationTable));
		
		session.execute(queryCreateKeyspace);
		session.execute(queryCreateObservationTable);
	}
	
}
