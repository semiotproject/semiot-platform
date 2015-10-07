package ru.semiot.platform.drivers.semiot.device.prototype;

import java.nio.ByteBuffer;

public class MessageParser {

	public static DevicePrototypeMessage parsePacket(byte[] res) {
		int index = 0;
		// uint8_t MAC[6];
		String mac = new String();
		boolean first = true;
		for (; index < 6; index++) {
			if (first) {
				mac = String.valueOf(getUInt8_t(res[index]));
				first = false;
			} else {
				mac += "." + String.valueOf(getUInt8_t(res[index]));
			}
		}
		System.out.println("MAC " + mac);
		
		index++; // пропускаем state
		float humidity = getFloat(res, index);
		System.out.println("Humidity "
				+ String.valueOf(humidity));
		float temperature = getFloat(res, index+=4);
		System.out.println("Temperature "
				+ String.valueOf(temperature));

		return new DevicePrototypeMessage(mac, humidity, temperature);
	}

	private static int getUInt8_t(byte value) {
		return ((int) value) & 0x000000FF;
	}

	private static float getFloat(byte[] value, int index) { 
	    return ByteBuffer.wrap(value, index, 4).getFloat();
	}
}
