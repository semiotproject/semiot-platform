package ru.semiot.platform.drivers.winghouse.machinetool;

public class MachineToolValue {
	private String mac;
	private MachineToolState machineToolState;
	private long timestamp;
	private boolean turnOn = true;
	private String hash; // потому что в осоновном идет работа со строками
	
	private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;

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
	
	public String getHash() {
		return hash;
	}
	
	public MachineToolValue calculateHash(String driverName) {
		String name = mac + driverName;
		int h = FNV_32_INIT;
        final int len = name.length();
        for(int i = 0; i < len; i++) {
        	h ^= name.charAt(i);
        	h *= FNV_32_PRIME;
        }
        long longHash = h & 0xffffffffl;
        hash = String.valueOf(longHash);
		return this;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof MachineToolValue) {
			return this.machineToolState == ((MachineToolValue) obj).getMachineToolState();
		}
		return false;
	}
}
