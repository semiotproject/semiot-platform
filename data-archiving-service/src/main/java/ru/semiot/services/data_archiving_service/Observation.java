package ru.semiot.services.data_archiving_service;

public class Observation {
	String sensor_id;
	String system_id;
	String event_time;
	String property;
	String feature_of_property;
	String observation_value;

	Observation(String sensor_id, String system_id, String event_time,
			String property, String feature_of_property,
			String observation_value) {
		this.sensor_id = sensor_id;
		this.system_id = system_id;
		this.event_time = event_time;
		this.property = property;
		this.feature_of_property = feature_of_property;
		this.observation_value = observation_value;
	}

	String insert() {
		return "INSERT INTO semiot.observation (sensor_id, system_id, "
				+ "event_time, property, feature_of_property, observation_value)"
				+ " VALUES ('" + sensor_id + "', '" + system_id + "','"
				+ event_time + "', '" + property + "', '" + feature_of_property
				+ "', '" + observation_value + "')";
	}

}
