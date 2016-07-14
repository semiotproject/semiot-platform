package ru.semiot.platform.deviceproxyservice.api.drivers;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;

public class Configuration extends HashMap<String, Object> {

  private boolean configured = false;

  public Configuration() {
    super();
  }

  public void putAll(Dictionary dictionary) {
    Enumeration dictKeys = dictionary.keys();

    while (dictKeys.hasMoreElements()) {
      Object key = dictKeys.nextElement();

      put(String.valueOf(key), String.valueOf(dictionary.get(key)));
    }
  }

  public boolean isConfigured() {
    return configured;
  }

  public void setConfigured() {
    this.configured = true;
  }

  public String getAsString(String key) {
    return get(key).toString();
  }

  public long getAsLong(String key) {
    return Long.valueOf(get(key).toString());
  }

  public int getAsInteger(String key) {
    return Integer.valueOf(get(key).toString());
  }

}
