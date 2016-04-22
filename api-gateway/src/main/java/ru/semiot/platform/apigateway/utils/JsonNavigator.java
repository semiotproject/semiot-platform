package ru.semiot.platform.apigateway.utils;

import com.github.jsonldjava.utils.JsonUtils;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonNavigator implements Iterator {

  private final Object json;

  private Iterator<Object> iterator;
  private DocumentContext path;

  private JsonNavigator(Object json) {
    this.json = json;

    if (json instanceof List) {
      this.iterator = ((List) json).iterator();
    } else if (json instanceof Map) {
      this.path = JsonPath.parse(json);
    }
  }

  public static JsonNavigator create(Object map) {
    return new JsonNavigator(map);
  }

  public static JsonNavigator create(String jsonString) throws IOException {
    return new JsonNavigator(JsonUtils.fromString(jsonString));
  }

  @Override
  public boolean hasNext() {
    if (iterator != null) {
      return iterator.hasNext();
    }
    throw new IllegalStateException("It's not an array!");
  }

  @Override
  public JsonNavigator next() {
    if (iterator != null) {
      return new JsonNavigator(iterator.next());
    }
    throw new IllegalStateException("It's not an array!");
  }

  public String readToString(String path) {
    if (path != null) {
      return this.path.read(path, String.class);
    }
    throw new IllegalStateException("It's not a map!");
  }

  public Map<String, Object> readToMap(String path) {
    return this.path.read(path, Map.class);
  }

}
