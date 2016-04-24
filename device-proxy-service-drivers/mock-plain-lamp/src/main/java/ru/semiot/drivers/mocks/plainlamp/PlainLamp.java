package ru.semiot.drivers.mocks.plainlamp;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;

import java.io.IOException;

public class PlainLamp extends Device {

  private static final String TEMPLATE_PATH = "/ru/semiot/drivers/mocks/plainlamp/description.ttl";
  private static String DESCRIPTION_TEMPLATE;

  static {
    try {
      DESCRIPTION_TEMPLATE = IOUtils.toString(PlainLamp.class
          .getResourceAsStream(TEMPLATE_PATH));
    } catch (IOException ex) {
      LoggerFactory.getLogger(PlainLamp.class)
          .error(ex.getMessage(), ex);
    }
  }

  public PlainLamp(String id) {
    super(id);
  }

  public String getRDFTemplate() {
    return DESCRIPTION_TEMPLATE;
  }
}
