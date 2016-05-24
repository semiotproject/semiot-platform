package ru.semiot.platform.deviceproxyservice.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.Observation;
import ru.semiot.platform.deviceproxyservice.api.drivers.RDFTemplate;

import java.io.IOException;

public class ObservationTest {

  @Test
  public void testEqualsIgnoreTimestamp() {
    Observation one = new ObservationImpl("1", "1", "1");
    Observation two = new ObservationImpl("1", "1" ,"2");

    assertTrue(one.equalsIgnoreTimestamp(two));
    assertTrue(two.equalsIgnoreTimestamp(one));
  }

  @Test
  public void testNotEqualsIgnoreTimestamp() {
    Observation one = new ObservationImpl("1", "1", "1");
    Observation two = new ObservationImpl("2", "1", "1");

    assertFalse(one.equalsIgnoreTimestamp(two));
    assertFalse(two.equalsIgnoreTimestamp(one));
  }

  private class ObservationImpl extends Observation {

    public ObservationImpl(String deviceId, String sensorId, String timestamp) {
      super(deviceId, sensorId, timestamp);
    }

    @Override
    public RDFTemplate getRDFTemplate() {
      try {
        return new RDFTemplate("observation", this.getClass()
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
    public RDFTemplate getRDFTemplate() {
      return null;
    }
  }

}
