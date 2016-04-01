package ru.semiot.services.tsdbservice.rest;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.services.tsdbservice.model.Observation;
import ru.semiot.services.tsdbservice.TSDBClient;
import rx.Observable;
import rx.Subscriber;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import java.io.StringWriter;

@Path("/observations")
public class Observations {

    private static final Logger logger = LoggerFactory
            .getLogger(Observations.class);
    public static final String GET_OBSERVATIONS_BY_START_END = "SELECT * FROM semiot.observation WHERE " +
            "system_id = '${SYSTEM_ID}' AND sensor_id = '${SENSOR_ID}' " +
            "AND event_time >= '${START}' AND event_time <= '${END}';";
    public static final String GET_OBSERVATIONS_BY_START = "SELECT * FROM semiot.observation WHERE " +
            "system_id = '${SYSTEM_ID}' AND sensor_id = '${SENSOR_ID}' " +
            "AND event_time >= '${START}';";
    public static final String GET_LATEST_OBSERVATION = "SELECT * FROM semiot.observation " +
            "WHERE system_id = '${SYSTEM_DI}' AND sensor_id = '${SENSOR_ID}' LIMIT 1";

    @GET
    @Path("test")
    public String test() {
        return "Test";
    }

    @GET
    public void getObservationsByRange(
            @Suspended final AsyncResponse response,
            @QueryParam("system_id") String systemId,
            @QueryParam("sensor_id") String sensorId,
            @QueryParam("start") String start,
            @QueryParam("end") String end) {
        logger.info(start);
        logger.info(end);

        if (StringUtils.isEmpty(systemId) || StringUtils.isEmpty(sensorId)
                || StringUtils.isEmpty(start)) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).build());
        } else {
            Observable<ResultSet> rs;
            if (StringUtils.isNoneEmpty(end)) {
                rs = TSDBClient.getInstance().executeAsync(
                        GET_OBSERVATIONS_BY_START_END
                                .replace("${SYSTEM_ID}", systemId)
                                .replace("${SENSOR_ID}", sensorId)
                                .replace("${START}", start)
                                .replace("${END}", end));
            } else {
                rs = TSDBClient.getInstance().executeAsync(
                        GET_OBSERVATIONS_BY_START
                                .replace("${SYSTEM_ID}", systemId)
                                .replace("${SENSOR_ID}", sensorId)
                                .replace("${START}", start));
            }

            rs.map((result) -> {
                Model observations = ModelFactory.createDefaultModel();
                result.forEach((row) -> {
                    Observation observation = new Observation(
                            row.getString("system_id"),
                            row.getString("sensor_id"),
                            row.getString("event_time"),
                            row.getString("property"),
                            row.getString("feature_of_interest"),
                            row.getString("observation_value"));

                    observations.add(observation.toRDF());
                });

                return observations;
            }).subscribe(new ResponseOrError(response));
        }
    }

    @GET
    @Path("/observations/latest")
    public void getObservationsLatest(
            @Suspended final AsyncResponse response,
            @QueryParam("system_id") String systemId,
            @QueryParam("sensor_id") String sensorId) {

        if (StringUtils.isEmpty(systemId) || StringUtils.isEmpty(sensorId)) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).build());
        } else {
            response.resume(Response.ok().build());
        }
    }

    private class ResponseOrError extends Subscriber<Model> {

        private final AsyncResponse response;

        ResponseOrError(AsyncResponse response) {
            this.response = response;
        }

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            logger.warn(e.getMessage(), e);
            if (e instanceof InvalidQueryException) {
                response.resume(Response.status(Response.Status.BAD_REQUEST).build());
            }
            response.resume(e);
        }

        @Override
        public void onNext(Model model) {
            StringWriter writer = new StringWriter();
            model.write(writer, RDFLanguages.strLangJSONLD);
            response.resume(writer.toString());
        }
    }
}
