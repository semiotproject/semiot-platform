package ru.semiot.platform.drivers.narodmon.temperature;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;

public class ScheduledDevice implements Runnable {
	
	private DeviceDriverImpl ddi;
	
	private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;
    private static final double maxTemperature = 50;
    
	
	private static String API_KEY = "21a2qYKfbqzyU";
	private static String UUID = "41e99f715d97f740cf34cdf146882fa9";
	private static String CMD_SENSOR_NEARBY = "sensorsNearby";
	private static final String templateTopic = "${DEVICE_HASH}";
	private static final String templateOnState = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "<http://${DOMAIN}/${SYSTEM_PATH}/${DEVICE_HASH}> saref:hasState saref:OnState.";
	
	/* String templateSubsystem = "ssn:hasSubSystem [ a ssn:SensingDevice ; "
			+ "ssn:observes qudt-quantity:ThermodynamicTemperature ; ssn:hasMeasurementCapability ["
			+ " a ssn:MeasurementCapability ; ssn:forProperty qudt-quantity:ThermodynamicTemperature ; "
			+ "ssn:hasMeasurementProperty [ a qudt:Unit ; ssn:hasValue [ a qudt:Quantity ; "
			+ "ssn:hasValue qudt-unit:DegreeCelsius ; ] ; ] ; ] ; ]"; */
	
	public ScheduledDevice(DeviceDriverImpl ddi) {
		this.ddi = ddi;
	}
	
	public void run() {
		long currentTimestamp = System.currentTimeMillis();
		try {
			List<Device> list = new ArrayList<Device>();
			URI uri = new URI(ddi.getUrl() + "/api");
			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost post = new HttpPost(uri);
			JSONObject json = new JSONObject();
			json.put("cmd", CMD_SENSOR_NEARBY).put("lat", "55.75").put("lng", "37.62")
				.put("radius", "20").put("types", "1").put("uuid", UUID).put("api_key", API_KEY);
			post.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));
			HttpResponse response = client.execute(post);
			json = new JSONObject(EntityUtils.toString(response.getEntity()));
	        JSONArray array = null;
	        
			if (json.has("devices")) {
	        	// список устройств найденных в радиусе 100 км
	            array = new JSONArray((json.get("devices")).toString());
	            for (int i = 0; i < array.length(); i++){          
                	// list sensors in device
	            	JSONObject jDevice = (JSONObject) array.get(i);
                    JSONArray a = new JSONArray(((JSONObject)array.get(i)).get("sensors").toString());
                    for(int j = 0; j< a.length(); j++){
                    	// one sensor
                    	JSONObject jSensor = a.getJSONObject(j);
                    	String hash = getHash(String.valueOf(jDevice.get("id")));
                    	
                    	String value = String.valueOf(jSensor.get("value"));
                    	Double val = Double.valueOf(value);
                    	if(maxTemperature > val) {
	                    	Device device = new Device(hash, "");
	                    	list.add(device);
	                    	System.out.println(hash + " " + value);
	                    	if(ddi.contains(device)) {
	                    		int index = ddi.listDevices().indexOf(device);
	                    		if(index > 0)
	                    		{
	                    			Device deviceOld = ddi.listDevices().get(index);
	                    			if(deviceOld != null && !deviceOld.getTurnOn()) {
	                    				deviceOld.setTurnOn(true);
	                    				ddi.inactiveDevice(templateOnState
	            	            				.replace("${DEVICE_HASH}", deviceOld.getID())
	            	            				.replace("${SYSTEM_PATH}", ddi.getPathSystemUri())
	            	            				.replace("${DOMAIN}", ddi.getDomain()));
	                    			}
	                    		}
	                    		sendMessage(value, currentTimestamp, hash);
	                    	}
	                    	else {
	                    		device.setRDFDescription(ddi.getTemplateDescription().replace(
									"${DEVICE_HASH}", hash).replace("${SYSTEM_PATH}", ddi.getPathSystemUri())
									.replace("${SENSOR_PATH}", ddi.getPathSensorUri())
									.replace("${SENSOR_ID}", "1")
									.replace("${DOMAIN}", ddi.getDomain())
									.replace("${LATITUDE}", String.valueOf(jDevice.get("lat")))
									.replace("${LONGITUDE}", String.valueOf(jDevice.get("lng"))));
	                    		ddi.addDevice(device);
	                    		sendMessage(value, currentTimestamp, hash);
	                    	}   
                    	} else {
                    		System.out.println("Sensor has invalid value.");
                    		System.out.println(jSensor.toString());
                    	}
                        break;
                    }
	            }
	            for(Device dev : ddi.listDevices()) {
	            	if(!list.contains(dev) && dev.getTurnOn()) {
	            		dev.setTurnOn(false);
	            		ddi.inactiveDevice(DeviceDriverImpl.templateOffState
	            				.replace("${DEVICE_HASH}", dev.getID())
	            				.replace("${SYSTEM_PATH}", ddi.getPathSystemUri())
	            				.replace("${DOMAIN}", ddi.getDomain()));
	            	}
	            }
	        }
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
	}
	
	private void sendMessage(String value, long timestamp, String hash) {
		if(value != null) {
			String topic = templateTopic.replace("${DEVICE_HASH}", hash);
	
			final String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
					.format(new Date(timestamp));

			String message = ddi
					.getTemplateObservation()
					.replace("${DOMAIN}", ddi.getDomain())
					.replace("${SYSTEM_PATH}", ddi.getPathSystemUri())
					.replace("${SENSOR_PATH}", ddi.getPathSensorUri())
					.replace("${DEVICE_HASH}", hash)
					.replace("${SENSOR_ID}", "1")
					.replace("${TIMESTAMP}", String.valueOf(timestamp))
					.replace("${DATETIME}", date)
					.replace("${VALUE}", value);
			
			ddi.publish(topic, message);
		} else {
			System.err.println(hash + " has unknown value (null)");
		}
	}
	
	private String getHash(String id) {
		String name = id + ddi.getDriverName();
		int h = FNV_32_INIT;
        final int len = name.length();
        for(int i = 0; i < len; i++) {
        	h ^= name.charAt(i);
        	h *= FNV_32_PRIME;
        }
        long longHash = h & 0xffffffffl;
        return String.valueOf(longHash);
	}
}
