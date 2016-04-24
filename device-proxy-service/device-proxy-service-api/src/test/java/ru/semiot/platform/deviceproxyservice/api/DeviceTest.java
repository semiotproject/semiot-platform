package ru.semiot.platform.deviceproxyservice.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;

public class DeviceTest {

  @Test
  public void testEquals() {
    Device one = new DeviceImpl("1");
    one.setProperty("test", "1");
    Device two = new DeviceImpl("1");
    two.setProperty("test", "1");

    assertEquals(one, two);

    Device three = new DeviceImpl("2");
    three.setProperty("test", "3");

    assertNotSame(two, three);
  }

  private class DeviceImpl extends Device {

    public DeviceImpl(String id) {
      super(id);
    }

    @Override
    public String getRDFTemplate() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

  }

}
