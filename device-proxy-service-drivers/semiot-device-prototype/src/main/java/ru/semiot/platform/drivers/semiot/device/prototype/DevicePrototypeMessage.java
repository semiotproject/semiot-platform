package ru.semiot.platform.drivers.semiot.device.prototype;

public class DevicePrototypeMessage {
	private String mac;
	private char state; // on/off state
	private float humidity;
	private float temperature;

	public DevicePrototypeMessage(String mac, float temperature, float humidity) {
		this.mac = mac;
		this.temperature = temperature;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}
	
	public void setHumidity(float humidity) {
		this.humidity = humidity;
	}
	
	public void setState(char state) {
		this.state = state;
	}

	public String getMac() {
		return mac;
	}

	public float getTemperature() {
		return temperature;
	}
	
	public float getHumidity() {
		return humidity;
	}

	public float getState() {
		return state;
	}
}
