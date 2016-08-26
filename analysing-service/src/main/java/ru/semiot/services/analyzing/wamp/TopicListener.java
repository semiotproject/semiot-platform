package ru.semiot.services.analyzing.wamp;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.StringReader;
import java.util.Calendar;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.jena.riot.RDFLanguages;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.services.analyzing.cep.Engine;
import rx.Observer;

@ApplicationScoped
@Named
public class TopicListener implements Observer<String> {

  private static final Logger logger = LoggerFactory
      .getLogger(TopicListener.class);
  @Inject
  Engine engine;

  public static final String TURTLE_TEMPLATE = "@prefix hmtr: <http://purl.org/NET/ssnext/heatmeters#> .\n"
      + "@prefix meter: <http://purl.org/NET/ssnext/meters/core#> .\n"
      + "@prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> .\n"
      + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n"
      + "@prefix qudt: <http://qudt.org/schema/qudt#> .\n"
      + "@prefix qudt-quantity: <http://qudt.org/vocab/quantity#> .\n"
      + "@prefix : <${SYSTEM_ID}/observations/temperature#> .\n"
      + "@prefix qudt-quantity-ext: <http://w3id.org/qudt/vocab/quantity/ext#> .\n"
      + "\n"
      + ":${TIMESTAMP} a ssn:Observation ;\n"
      + "    ssn:observedProperty ${OBS_TYPE} ;\n"
      + "    ssn:observedBy <${SYSTEM_ID}-temperature> ;\n"
      + "    ssn:observationResultTime \"${OBS_TIME}\"^^xsd:dateTime ;\n"
      + "    ssn:observationResult :${TIMESTAMP}-result .\n"
      + "\n"
      + ":${TIMESTAMP}-result a ssn:SensorOutput ;\n"
      + "    ssn:isProducedBy <${SYSTEM_ID}-temperature> ;\n"
      + "    ssn:hasValue :${TIMESTAMP}-resultvalue .\n"
      + "\n"
      + ":${TIMESTAMP}-resultvalue a qudt:QuantityValue ;\n"
      + "    qudt:quantityValue \"${VALUE}\"^^xsd:double .";

  public TopicListener() {
    logger.info("Created Listener");
  }

  @Override
  public void onCompleted() {
    logger.info("Completed");
  }

  @Override
  public void onError(Throwable e) {
    logger.warn(e.getMessage(), e);
  }

  @Override
  public void onNext(String message) {
    if (message.isEmpty()) {
      return;
    }
    try {
      JSONObject json = new JSONObject(message);
      String type = json.getString("ssn:observedProperty");
      if (type.startsWith("http://") || type.startsWith("https://")) {
        type = "<" + type + ">";
      }
      String value = json.getJSONObject("ssn:observationResult").getJSONObject("ssn:hasValue").get("qudt:quantityValue").toString().replace(",", ".");
      String time = json.getString("ssn:observationResultTime");
      String sensor = json.getString("ssn:observedBy");
      sensor = sensor.substring(0, sensor.lastIndexOf("-"));
      long tmsmp = Calendar.getInstance().getTime().getTime();
      String msg = TURTLE_TEMPLATE.replace("${SYSTEM_ID}", sensor).replace("${VALUE}", value)
          .replace("${OBS_TIME}", time)
          .replace("${TIMESTAMP}", String.valueOf(tmsmp))
          .replace("${OBS_TYPE}", type);
      Model description = ModelFactory.createDefaultModel().read(
          new StringReader(msg), null, RDFLanguages.strLangTurtle);
      if (!description.isEmpty()) {
        engine.appendData(description);
      }
    } catch (JSONException ex) {
      System.out.println("Oops");
    }
  }

}
