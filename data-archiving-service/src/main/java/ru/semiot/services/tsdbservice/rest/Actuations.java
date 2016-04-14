package ru.semiot.services.tsdbservice.rest;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.UDTValue;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.restapi.MediaType;
import ru.semiot.services.tsdbservice.TSDBClient;
import ru.semiot.services.tsdbservice.model.Actuation;
import rx.Observable;
import rx.functions.Func1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.semiot.services.tsdbservice.rest.ResponseModelOrError.responseModelOrError;

@Path("/actuations")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_LD_JSON})
public class Actuations {

    private static final Logger logger = LoggerFactory.getLogger(Actuations.class);
    private static final String GET_ACTUATIONS_BY_START =
            "SELECT * FROM semiot.actuation WHERE " +
                    "system_id = '${SYSTEM_ID}' AND event_time >= '${START}';";
    private static final String GET_ACTUATIONS_BY_START_END =
            "SELECT * FROM semiot.actuation WHERE " +
                    "system_id = '${SYSTEM_ID}' " +
                    "AND event_time >= '${START}' " +
                    "AND event_time <= '${END}';";
    private static final String GET_LATEST_ACTIVATION =
            "SELECT * FROM semiot.actuation WHERE " +
                    "system_id = '${SYSTEM_ID}' " +
                    "LIMIT 1";

    @GET
    public void getActuationsByRange(@Suspended AsyncResponse response,
                                     @QueryParam("system_id") String systemId,
                                     @QueryParam("start") ZonedDateTime start,
                                     @QueryParam("end") ZonedDateTime end) {
        if (Strings.isNullOrEmpty(systemId) || start == null) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).build());
        }

        Observable<ResultSet> rs;
        if (end == null) {
            rs = TSDBClient.getInstance().executeAsync(GET_ACTUATIONS_BY_START
                    .replace("${SYSTEM_ID}", systemId)
                    .replace("${START}", start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        } else {
            rs = TSDBClient.getInstance().executeAsync(GET_ACTUATIONS_BY_START
                    .replace("${SYSTEM_ID}", systemId)
                    .replace("${START}", start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                    .replace("${END}", end.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        }

        rs.map(new ResultSetToModel())
                .subscribe(responseModelOrError(response));
    }

    @GET
    @Path("/latest")
    public void getLatestActuation(@Suspended AsyncResponse response,
                                   @QueryParam("system_id") String systemId) {
        if (Strings.isNullOrEmpty(systemId)) {
            response.resume(Response.status(Response.Status.NOT_FOUND).build());
        }

        TSDBClient.getInstance().executeAsync(GET_LATEST_ACTIVATION
                .replace("${SYSTEM_ID}", systemId))
                .map(new ResultSetToModel())
                .subscribe(responseModelOrError(response));
    }

    private class ResultSetToModel implements Func1<ResultSet, Model> {

        @Override
        public Model call(ResultSet resultSet) {
            Model actuations = ModelFactory.createDefaultModel();
            resultSet.forEach((row) -> {
                Actuation actuation = new Actuation(
                        row.getString("system_id"),
                        row.getTimestamp("event_time").toInstant().toString(),
                        row.getString("command_type")
                );

                List<UDTValue> properties = row.getList("command_properties", UDTValue.class);
                actuation.addProperties(properties);

                actuations.add(actuation.toRDF());
            });

            return actuations;
        }
    }
}
