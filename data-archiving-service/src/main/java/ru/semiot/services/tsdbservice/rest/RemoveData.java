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
      + "semiot.commandresult WHERE system_id IN (${SYSTEMS});";
  private static final String TEMPLATE_SYSTEM = "'${SYSTEM_ID}'";
  private static final String JSON_SYSTEM_ID = "system_id";
  private static final String JSON_SENSOR_ID = "sensor_id";
  
  @POST
  public void removeMetric(@Suspended final AsyncResponse response, String message) {
    StringReader reader = new StringReader(message);
    JsonReader js = Json.createReader(reader);
    JsonArray jArray = js.readArray();

    List<String> queries = new ArrayList<String>();
    List<String> systems = new ArrayList<String>();
    for (int i = 0; i < jArray.size(); i++) {
      JsonObject val = jArray.getJsonObject(i);
      String system_id = val.getString(JSON_SYSTEM_ID);
      String sensor_id = val.getString(JSON_SENSOR_ID);
      queries.add(DELETE_OBSERVATION
          .replace("${SYSTEM_ID}", system_id)
          .replace("${SENSOR_ID}", sensor_id));
      String system = TEMPLATE_SYSTEM.replace("${SYSTEM_ID}", system_id);
      if (!systems.contains(system)) {
        systems.add(system);
      }
    }

    if (!queries.isEmpty()) {
      Observable<ResultSet> rsObs = TSDBClient.getInstance().executeAsync(queries);
      rsObs.map((result) -> result.toString()).subscribe(responseStringOrError(response));
    }

    if (!systems.isEmpty()) {
      String deleteCommandresults =
          DELETE_COMMANDRESULT.replace("${SYSTEMS}", String.join(",", systems));
      Observable<ResultSet> rsCommres = TSDBClient.getInstance().executeAsync(deleteCommandresults);
      rsCommres.map((result) -> result.toString()).subscribe(responseStringOrError(response));
    }
  }

}
