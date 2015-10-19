package ru.semiot.platform.drivers.winghouse.machinetool;

public class MachineToolValue {
	private String mac;
	private MachineToolState machineToolState;
	private long timestamp;
	private boolean turnOn = true;

	public MachineToolValue(String mac, MachineToolState machineToolState, long timestamp) {
		this.mac = mac;
		this.machineToolState = machineToolState;
		this.timestamp = timestamp;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public void setMachineToolState(MachineToolState machineToolState) {
		this.machineToolState = machineToolState;
	}

	public void setTimestemp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setTurnOn(boolean turnOn) {
		this.turnOn = turnOn;
	}
	
	public String getMac() {
		return mac;
	}

	public MachineToolState getMachineToolState() {
		return machineToolState;
	}
	
	public long getTimestemp() {
		return timestamp;
	}
	
	public boolean getTurnOn() {
		return turnOn;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof MachineToolValue) {
			return this.machineToolState == ((MachineToolValue) obj).getMachineToolState();
		}
		return false;
	}
}
