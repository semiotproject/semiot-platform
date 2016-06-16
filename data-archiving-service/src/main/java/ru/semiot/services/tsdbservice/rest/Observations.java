package ru.semiot.services.tsdbservice.rest;

import static ru.semiot.services.tsdbservice.rest.ResponseModelOrError.responseModelOrError;
import static ru.semiot.services.tsdbservice.rest.ResponseStringOrError.responseStringOrError;

import com.datastax.driver.core.ResultSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.services.tsdbservice.TSDBClient;
import ru.semiot.services.tsdbservice.model.Observation;
import rx.Observable;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;

@Path("/observations")
public class Observations {

  private static final Logger logger = LoggerFactory
      .getLogger(Observations.class);
  
  public static final String GET_LATEST_OBSERVATION = "SELECT * FROM semiot.observation "
      + "WHERE system_id = '${SYSTEM_ID}' AND sensor_id IN (${SENSOR_IDS}) LIMIT 1";
  public static final String GET_OBSERVATIONS_BY_START_END =
      "SELECT * FROM semiot.observation WHERE "
          + "event_time >= '${START}' AND event_time <= '${END}' "
          + "AND system_id = '${SYSTEM_ID}' AND sensor_id IN (${SENSOR_IDS});";
  public static final String GET_OBSERVATIONS_BY_START = "SELECT * FROM semiot.observation WHERE "
      + "event_time >= '${START}' AND system_id = '${SYSTEM_ID}' AND sensor_id IN (${SENSOR_IDS});";
  public static final String SENSOR_ID = "'${SENSOR_ID}'";

  @GET
  @Path("test")
  public String test() {
    return "Test";
  }

  @GET
  public void getObservationsByRange(@Suspended final AsyncResponse response,
      @QueryParam("system_id") String systemId, @QueryParam("sensor_id") List<String> listSensorId,
      @QueryParam("start") String start, @QueryParam("end") String end) {
    if (StringUtils.isEmpty(systemId) || listSensorId.isEmpty() || StringUtils.isEmpty(start)) {
      response.resume(Response.status(Response.Status.BAD_REQUEST).build());
    } else {
      Observable<ResultSet> rs;
      String getObservations;
      if (StringUtils.isNoneEmpty(end)) {
        getObservations = GET_OBSERVATIONS_BY_START_END
            .replace("${SYSTEM_ID}", systemId)
            .replace("${START}", start).replace("${END}", end);
      } else {
        getObservations = GET_OBSERVATIONS_BY_START
            .replace("${SYSTEM_ID}", systemId)
            .replace("${START}", start);
      }

      rs = TSDBClient.getInstance().executeAsync(getObservations
          .replace("${SYSTEM_ID}", systemId)
          .replace("${SENSOR_IDS}", toSensors(listSensorId)));
      resultSetToRfd(response, rs);
    }
  }

  @GET
  @Path("/latest")
  public void getObservationsLatest(@Suspended final AsyncResponse response,
      @QueryParam("system_id") String systemId,
      @QueryParam("sensor_id") List<String> listSensorId) {

    if (StringUtils.isEmpty(systemId) || listSensorId.isEmpty()) {
      response.resume(Response.status(Response.Status.BAD_REQUEST).build());
    } else {
      Observable<ResultSet> rs = TSDBClient.getInstance().executeAsync(GET_LATEST_OBSERVATION
          .replace("${SYSTEM_ID}", systemId)
          .replace("${SENSOR_IDS}", toSensors(listSensorId)));

      resultSetToRfd(response, rs);
    }
  }

  @GET
  @Path("/latest/time")
  public void getTimeObservationsLatest(@Suspended final AsyncResponse response,
      @QueryParam("system_id") String systemId,
      @QueryParam("sensor_id") List<String> listSensorId) {

    if (StringUtils.isEmpty(systemId) || listSensorId.isEmpty()) {
      response.resume(Response.status(Response.Status.BAD_REQUEST).build());
    } else {
      Observable<ResultSet> rs = TSDBClient.getInstance().executeAsync(GET_LATEST_OBSERVATION
          .replace("${SYSTEM_ID}", systemId)
          .replace("${SENSOR_IDS}", toSensors(listSensorId)));

      rs.map((result) -> {
        if(!result.isExhausted()) {
          return result.one().getTimestamp("event_time").toInstant().toString();
        } else {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
      }).subscribe(responseStringOrError(response));
    }
  }

  private String toSensors(List<String> listSensorId) {
    StringBuilder partitionKeys = new StringBuilder();
    boolean isFirst = true;
    for (String sensorId : listSensorId) {
      if (isFirst) {
        isFirst = false;
      } else {
        partitionKeys.append(", ");
      }
      partitionKeys.append(SENSOR_ID.replace("${SENSOR_ID}", sensorId));
    }
    return partitionKeys.toString();
  }

  private void resultSetToRfd(final AsyncResponse response,
      Observable<ResultSet> rs) {
    rs.map((result) -> {
      Model observations = ModelFactory.createDefaultModel();
      result.forEach((row) -> {
        Observation observation = new Observation(
            row.getString("system_id"), row.getString("sensor_id"),
            row.getTimestamp("event_time").toInstant().toString(),
            row.getString("property"),
            row.getString("feature_of_interest"),
            row.getString("value"));

        observations.add(observation.toRDF());
      });

      return observations;
    }).subscribe(responseModelOrError(response));
  }
  
}
