package ru.semiot.commons.rdf;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class ModelJsonLdUtils {

  private static final JsonLdOptions DEFAULT_OPTIONS = new JsonLdOptions();
  private static final String JSONPATH_BN_OBJECTS = "$..[?(@.@id =~ /_:.*/i)]";
  private static final String JSONLD_KEY_ID = "@id";

  public static Object toJsonLd(Model model) throws IOException {
    StringWriter writer = new StringWriter();
    model.write(writer, Lang.JSONLD.getName());

    return JsonUtils.fromString(writer.toString());
  }

  public static Object toJsonLdCompact(Model model, Object frame)
      throws JsonLdError, IOException {
    return deleteRedundantBNIds(JsonLdProcessor.compact(JsonLdProcessor.frame(
        ModelJsonLdUtils.toJsonLd(model), frame, DEFAULT_OPTIONS), frame, DEFAULT_OPTIONS));
  }

  public static Object deleteRedundantBNIds(Object json) throws IOException {
    String json_str = JsonUtils.toString(json);
    DocumentContext path = JsonPath.parse(json);

    JSONArray bnResources = path.read(JSONPATH_BN_OBJECTS);

    bnResources.stream()
        .map((resource) -> (Map<String, Object>) resource)
        .map((Map<String, Object> m) -> (String) m.get(JSONLD_KEY_ID))
        .filter((bnId) -> (StringUtils.countMatches(json_str, "\"" + bnId + "\"") < 2))
        .forEach((bnId) -> path.delete("$..*[?(@.@id=~/" + bnId + "/i)].@id"));

    return path.json();
  }
}
