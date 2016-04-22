import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import ru.semiot.platform.deviceproxyservice.api.drivers.Observation;

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
      throw new UnsupportedOperationException("Not supported yet.");
    }

  }

}
