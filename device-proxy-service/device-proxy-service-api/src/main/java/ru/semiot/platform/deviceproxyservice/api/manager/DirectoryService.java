package ru.semiot.platform.deviceproxyservice.api.manager;

import org.apache.jena.rdf.model.Model;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DriverInformation;

import java.net.URI;

public interface DirectoryService {

  public void loadDevicePrototype(URI uri);

  /**
   * Doesn't check whether device already exists.
   *
   * @return true if the given device successfully added.
   */
  public boolean addNewDevice(DriverInformation info, Device device, Model description);

  public boolean updateDevice(DriverInformation info, Device device, Model description);

  public String findDriverPidByDeviceId(String deviceId);

}
