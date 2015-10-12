package ru.semiot.platform.drivers.winghouse.machinetool;

public class MachineToolMessage {
	private String mac;
	private MachineToolState machineToolState;

	public MachineToolMessage(String mac, MachineToolState machineToolState) {
		this.mac = mac;
		this.machineToolState = machineToolState;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public void setMachineToolState(MachineToolState machineToolState) {
		this.machineToolState = machineToolState;
	}

	public String getMac() {
		return mac;
	}

	public MachineToolState getMachineToolState() {
		return machineToolState;
	}

}
