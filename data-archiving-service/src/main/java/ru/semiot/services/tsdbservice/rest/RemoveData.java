package ru.semiot.services.tsdbservice.rest;

import static ru.semiot.services.tsdbservice.rest.ResponseStringOrError.responseStringOrError;

import com.datastax.driver.core.ResultSet;
import ru.semiot.services.tsdbservice.TSDBClient;
import rx.Observable;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

@Path("/remove")
public class RemoveData {
  
  private static final String DELETE_OBSERVATION = "DELETE FROM "
      + "semiot.observation WHERE system_id = '${SYSTEM_ID}' "
      + "AND sensor_id = '${SENSOR_ID}';";
  private static final String DELETE_COMMANDRESULT = "DELETE FROM "
      + "semiot.commandresult WHERE system_id = '${SYSTEM_ID}' "
      + "AND process_id = '${PROCESS_ID}';";
  private static final String JSON_SYSTEM_ID = "system_id";
  private static final String JSON_SENSOR_ID = "sensor_id";
  private static final String JSON_PROCESS_ID = "process_id";
  
  @POST
  public void removeMetric(@Suspended final AsyncResponse response, String message) {
    StringReader reader = new StringReader(message);
    JsonReader js = Json.createReader(reader);
    JsonObject jObj = js.readObject();
    JsonArray observations = jObj.getJsonArray("observations");
    JsonArray commandresults = jObj.getJsonArray("commandresults");

    List<String> queries = new ArrayList<String>();
    for (int i = 0; i < observations.size(); i++) {
      JsonObject val = observations.getJsonObject(i);
      String system_id = val.getString(JSON_SYSTEM_ID);
      String sensor_id = val.getString(JSON_SENSOR_ID);
      queries.add(DELETE_OBSERVATION
          .replace("${SYSTEM_ID}", system_id)
          .replace("${SENSOR_ID}", sensor_id));
    }
    for (int i = 0; i < commandresults.size(); i++) {
      JsonObject val = commandresults.getJsonObject(i);
      String system_id = val.getString(JSON_SYSTEM_ID);
      String process_id = val.getString(JSON_PROCESS_ID);
      queries.add(DELETE_COMMANDRESULT
          .replace("${SYSTEM_ID}", system_id)
          .replace("${PROCESS_ID}", process_id));
    }

    if (!queries.isEmpty()) {
      Observable<ResultSet> rsObs = TSDBClient.getInstance().executeAsync(queries);
      rsObs.map((result) -> result.toString()).subscribe(responseStringOrError(response));
    }
  }

}
