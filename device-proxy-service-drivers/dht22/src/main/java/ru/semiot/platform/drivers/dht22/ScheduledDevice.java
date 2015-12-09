package ru.semiot.platform.drivers.dht22;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

import ru.semiot.platform.deviceproxyservice.api.drivers.Device;

public class ScheduledDevice implements Runnable {
	
	private DeviceDriverImpl ddi;
	
	private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;
    
	private CoapClient coapClientTemperature;
	private CoapClient coapClientHumidity;
	private String hash;
	
	private static final String templateTopic = "${DEVICE_HASH}";
	private static final String templateOnState = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "<http://${DOMAIN}/${SYSTEM_PATH}/${DEVICE_HASH}> saref:hasState saref:OnState.";
	
	public ScheduledDevice(DeviceDriverImpl ddi) {
		this.ddi = ddi;
		hash = getHash(ddi.getDriverName() + ddi.getIp() + String.valueOf(ddi.getPort()));
        ddi.addDevice(new Device(hash, ddi.getTemplateDescription()
        		.replace("${DEVICE_HASH}", hash).replace("${SYSTEM_PATH}", ddi.getPathSystemUri())
        		.replace("${SENSOR_PATH}", ddi.getPathSensorUri())
				.replace("${DOMAIN}", ddi.getDomain())
				.replace("${LATITUDE}", String.valueOf(ddi.getLat()))
				.replace("${LONGITUDE}", String.valueOf(ddi.getLng()))));
	}
	
	public void createCoapClients() {
		coapClientHumidity = new CoapClient("coap://" + ddi.getIp() + ":" + 
				String.valueOf(ddi.getPort()) + "/dht22/humidity");
		coapClientTemperature = new CoapClient("coap://" + ddi.getIp() + ":" + 
				String.valueOf(ddi.getPort()) + "/dht22/temperature");
		coapClientTemperature.setEndpoint(CoAPInterface.getEndpoint());
        coapClientHumidity.setEndpoint(CoAPInterface.getEndpoint());
	}
	
	public void run() {
		if(ddi.listDevices().size() == 1 &&  ddi.listDevices().get(0).getTurnOn()) {
			long timestamp = System.currentTimeMillis();
			CoapResponse crHumidity = coapClientHumidity.get();
			System.out.println(crHumidity.getResponseText());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
			CoapResponse crTemperature = coapClientTemperature.get();
			System.out.println(crTemperature.getResponseText());
				
			if(crTemperature != null && crHumidity != null) {
				Device device = ddi.listDevices().get(0);
				if(!device.getTurnOn()) {
					device.setTurnOn(true);
					ddi.inactiveDevice(templateOnState
		    				.replace("${DEVICE_HASH}", device.getID())
		    				.replace("${SYSTEM_PATH}", ddi.getPathSystemUri())
		    				.replace("${DOMAIN}", ddi.getDomain()));
				}
				sendMessage(crTemperature.getResponseText().trim(), crHumidity.getResponseText().trim(),
						timestamp, hash);
			} else { // TODO проверить является ли это фактором отключения?
				Device device = ddi.listDevices().get(0);
				device.setTurnOn(false);
	    		ddi.inactiveDevice(DeviceDriverImpl.templateOffState
	    				.replace("${DEVICE_HASH}", device.getID())
	    				.replace("${SYSTEM_PATH}", ddi.getPathSystemUri())
	    				.replace("${DOMAIN}", ddi.getDomain()));
	    		
	    		coapClientTemperature.shutdown();
	    		coapClientHumidity.shutdown();
	    		ddi.restartSheduller();
			}
		}
	}
	
	private void sendMessage(String valueTemperature, String valueHumidity,
			long timestamp, String hash) {
		if(valueTemperature != null && valueHumidity != null) {
			String topic = templateTopic.replace("${DEVICE_HASH}", hash);
	
			final String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
					.format(new Date(timestamp));

			String messageTemperature = ddi
					.getTemplateObservationTemperature()
					.replace("${DOMAIN}", ddi.getDomain())
					.replace("${SYSTEM_PATH}", ddi.getPathSystemUri())
					.replace("${SENSOR_PATH}", ddi.getPathSensorUri())
					.replace("${DEVICE_HASH}", hash)
					.replace("${TIMESTAMP}", String.valueOf(timestamp))
					.replace("${DATETIME}", date)
					.replace("${VALUE}", valueTemperature);
			
			String messageHumidity = ddi
					.getTemplateObservationHumidity()
					.replace("${DOMAIN}", ddi.getDomain())
					.replace("${SYSTEM_PATH}", ddi.getPathSystemUri())
					.replace("${SENSOR_PATH}", ddi.getPathSensorUri())
					.replace("${DEVICE_HASH}", hash)
					.replace("${TIMESTAMP}", String.valueOf(timestamp))
					.replace("${DATETIME}", date)
					.replace("${VALUE}", valueHumidity);
			
			ddi.publish(topic, messageTemperature);
			ddi.publish(topic, messageHumidity);
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
