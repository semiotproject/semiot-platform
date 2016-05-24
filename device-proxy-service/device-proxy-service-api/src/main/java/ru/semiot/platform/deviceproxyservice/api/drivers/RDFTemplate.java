package ru.semiot.platform.deviceproxyservice.api.drivers;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class RDFTemplate {

  private static final MustacheFactory MUSTACHE_FACTORY = new DefaultMustacheFactory();
  private static final Pattern PATTERN_VARIABLE =
      Pattern.compile(".*\\{\\{.*\\}\\}.*", Pattern.DOTALL);
  private final String name;
  private final String template;
  private final String lang = "Turtle";

  public RDFTemplate(String name, InputStream template) throws IOException {
    this(name, IOUtils.toString(template));
  }

  public RDFTemplate(String name, String template) {
    this.name = name;
    this.template = template;
  }

  public String getTemplateString() {
    return template;
  }

  public String getRDFLanguage() {
    return lang;
  }

  public String resolveToString(Map<String, Object>... maps) {
    Map<String, Object> scope = new FailOnMissingMapWrapper(mergeMaps(maps));
    String tmp = new String(template);
    for (int i = 0; PATTERN_VARIABLE.matcher(tmp).matches(); i++) {
      StringWriter writer = new StringWriter();
      Mustache mustache = MUSTACHE_FACTORY.compile(new StringReader(tmp), name + i);
      mustache.execute(writer, scope);
      tmp = writer.toString();
    }
    return tmp;
  }

  public StringReader resolveToReader(Map<String, Object>... maps) {
    return new StringReader(resolveToString(maps));
  }

  private Map<String, Object> mergeMaps(Map<String, Object>... maps) {
    Map<String, Object> result = new HashMap<>();

    for (Map<String, Object> map : maps) {
      result.putAll(map);
    }

    return result;
  }

  private class FailOnMissingMapWrapper extends AbstractMap<String, Object> {

    private final Map<String, Object> map;

    FailOnMissingMapWrapper(Map<String, Object> map) {
      this.map = map;
    }

    @Override
    public boolean containsKey(Object key) {
      if (!map.containsKey(key)) {

        print();
        throw new IllegalStateException("Missing key: '" + key + "'");
      }
      return map.containsKey(key);
    }

    @Override
    public Object get(Object key) {
      if (!map.containsKey(key)) {

        print();
        throw new IllegalStateException("Missing key: '" + key + "'");
      }
      return map.get(key);
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
      return map.entrySet();
    }

    private void print() {
      System.out.println("Map:");
      for (String key : map.keySet()) {
        System.out.println(key + ":" + map.get(key));
      }
    }
  }
}
