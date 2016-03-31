package ru.semiot.services.data_archiving_service;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestServiceImpl {

	private static final Logger logger = LoggerFactory
			.getLogger(RestServiceImpl.class);
	
	@POST
	@Path("/remove/metric")
	public String removeMetric(String message) {
		JsonObject jObj = JSON.parse(message);
		JsonArray jarray = jObj.get("metrics").getAsArray();
		for (int i = 0; i < jarray.size(); i++) {
			String metric = jarray.get(i).getAsString().value();
			String res = TsdbQueryUtil
					.deleteMetricRequest(metric);
			if(res != null) {
				logger.info("Metric {} has been removed", metric);
			} else {
				logger.warn("Metric {} has not been removed", metric);
			}
		}
		return "Removing metrics completed";
	}

	@POST
	@Path("/observations/latest")
	public void getObservationLatest(
			@Suspended final AsyncResponse response,
			@QueryParam("sensor_id") String sensorId) {

	}

    @POST
    @Path("/observations")
    public void getObservationByRange(
            @Suspended final AsyncResponse response,
            @QueryParam("sensor_id") String sensorId,
            @QueryParam("start") String start,
            @QueryParam("end") String end) {

    }
}
