package ru.semiot.drivers.mocks.plainlamp;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.RDFTemplate;

import java.io.IOException;

public class PlainLamp extends Device {

  private static final String TEMPLATE_PATH = "/ru/semiot/drivers/mocks/plainlamp/description.ttl";
  private static RDFTemplate TEMPLATE_DESCRIPTION;
  private boolean isOn = false;
  private int lumen = 890;
  private int kelvin = 4000;

  static {
    try {
      TEMPLATE_DESCRIPTION = new RDFTemplate("description",
          PlainLamp.class.getResourceAsStream(TEMPLATE_PATH));
    } catch (IOException ex) {
      LoggerFactory.getLogger(PlainLamp.class).error(ex.getMessage(), ex);
    }
  }

  public PlainLamp(String id) {
    super(id);
  }

  public boolean getIsOn() {
    return isOn;
  }

  public void setIsOn(boolean isOn) {
    this.isOn = isOn;
  }

  public int getKelvin() {
    return kelvin;
  }

  public int getLumen() {
    return lumen;
  }

  public void setLumen(int lumen) {
    this.lumen = lumen;
  }

  public void setKelvin(int kelvin) {
    this.kelvin = kelvin;
  }

  public RDFTemplate getRDFTemplate() {
    return TEMPLATE_DESCRIPTION;
  }
}
