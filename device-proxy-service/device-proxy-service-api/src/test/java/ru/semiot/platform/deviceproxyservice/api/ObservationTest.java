package ru.semiot.platform.deviceproxyservice.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.RDFLanguages;
import org.junit.Test;
import ru.semiot.platform.deviceproxyservice.api.drivers.Configuration;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.Observation;

import java.io.IOException;

public class ObservationTest {

  @Test
  public void testEqualsIgnoreTimestamp() {
    Observation one = new ObservationImpl("1", "1");
    Observation two = new ObservationImpl("1", "2");

    assertTrue(one.equalsIgnoreTimestamp(two));
    assertTrue(two.equalsIgnoreTimestamp(one));
  }

  @Test
  public void testNotEqualsIgnoreTimestamp() {
    Observation one = new ObservationImpl("1", "1");
    Observation two = new ObservationImpl("2", "1");

    assertFalse(one.equalsIgnoreTimestamp(two));
    assertFalse(two.equalsIgnoreTimestamp(one));
  }

  private class ObservationImpl extends Observation {

    public ObservationImpl(String deviceId, String timestamp) {
      super(deviceId, timestamp);
    }

    @Override
    public String getRDFTemplate() {
      try {
        return IOUtils.toString(this.getClass()
            .getResourceAsStream("/ru/semiot/platform/deviceproxyservice/api/observation.ttl"));
      } catch (IOException e) {
        e.printStackTrace();

        return null;
      }
    }

  }

  private class DeviceImpl extends Device {

    public DeviceImpl(String id) {
      super(id);
    }

    @Override
    public String getRDFTemplate() {
      return null;
    }
  }

}
