package ru.semiot.services.tsdbservice.rest;

import static ru.semiot.services.tsdbservice.rest.ResponseModelOrError.responseModelOrError;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.UDTValue;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.restapi.MediaType;
import ru.semiot.services.tsdbservice.TSDBClient;
import ru.semiot.services.tsdbservice.model.CommandResult;
import rx.Observable;
import rx.functions.Func1;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;

@Path("/commandResults")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_LD_JSON})
public class CommandResults {

  private static final Logger logger = LoggerFactory.getLogger(CommandResults.class);
  private static final String GET_COMMANDRESULTS_BY_START =
      "SELECT * FROM semiot.commandresult WHERE " +
          "system_id = '${SYSTEM_ID}' AND process_id = '${PROCESS_ID}' " +
          "AND event_time >= '${START}';";
  private static final String GET_COMMANDRESULTS_BY_START_END =
      "SELECT * FROM semiot.commandresult WHERE " +
          "system_id = '${SYSTEM_ID}' " +
          "AND process_id = '${PROCESS_ID}' " +
          "AND event_time >= '${START}' " +
          "AND event_time <= '${END}';";
  private static final String GET_LATEST_COMMANDRESULT =
      "SELECT * FROM semiot.commandresult WHERE " +
          "system_id = '${SYSTEM_ID}' " +
          "AND process_id = '${PROCESS_ID}' " +
          "LIMIT 1";
  private static final String VAR_SYSTEM_ID = "${SYSTEM_ID}";
  private static final String VAR_PROCESS_ID = "${PROCESS_ID}";
  private static final String VAR_START = "${START}";
  private static final String VAR_END = "${END}";

  @GET
  public void getCommandResultsByRange(@Suspended AsyncResponse response,
      @QueryParam("system_id") String systemId, @QueryParam("process_id") String processId,
      @QueryParam("start") ZonedDateTime start, @QueryParam("end") ZonedDateTime end) {
    if (Strings.isNullOrEmpty(systemId) || Strings.isNullOrEmpty(processId) || start == null) {
      response.resume(Response.status(Response.Status.BAD_REQUEST).build());
    }

    Observable<ResultSet> rs;
    if (end == null) {
      rs = TSDBClient.getInstance().executeAsync(GET_COMMANDRESULTS_BY_START
          .replace(VAR_SYSTEM_ID, systemId)
          .replace(VAR_PROCESS_ID, processId)
          .replace(VAR_START, start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
    } else {
      rs = TSDBClient.getInstance().executeAsync(GET_COMMANDRESULTS_BY_START_END
          .replace(VAR_SYSTEM_ID, systemId)
          .replace(VAR_PROCESS_ID, processId)
          .replace(VAR_START, start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
          .replace(VAR_END, end.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
    }

    rs.map(new ResultSetToModel()).subscribe(responseModelOrError(response));
  }

  @GET
  @Path("/latest")
  public void getLatestCommandResult(@Suspended AsyncResponse response,
      @QueryParam("system_id") String systemId, @QueryParam("process_id") String processId) {
    if (Strings.isNullOrEmpty(systemId) || Strings.isNullOrEmpty(processId)) {
      response.resume(Response.status(Response.Status.BAD_REQUEST).build());
    }

    TSDBClient.getInstance().executeAsync(
        GET_LATEST_COMMANDRESULT
            .replace(VAR_SYSTEM_ID, systemId)
            .replace(VAR_PROCESS_ID, processId))
        .map(new ResultSetToModel())
        .subscribe(responseModelOrError(response));
  }

  private class ResultSetToModel implements Func1<ResultSet, Model> {

    @Override
    public Model call(ResultSet resultSet) {
      Model commandResults = ModelFactory.createDefaultModel();
      resultSet.forEach((row) -> {
        CommandResult commandResult = new CommandResult(
            row.getString("system_id"),
            row.getString("process_id"),
            row.getTimestamp("event_time").toInstant().toString(),
            row.getString("command_type")
        );

        List<UDTValue> properties = row.getList("command_properties", UDTValue.class);
        commandResult.addProperties(properties);

        List<UDTValue> parameters = row.getList("command_parameters", UDTValue.class);
        commandResult.addParameters(parameters);

        commandResults.add(commandResult.toRDF());
      });

      return commandResults;
    }
  }
}
