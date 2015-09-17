package ru.semiot.platform.drivers.winghouse.machinetool;

public class MachineToolMessage {
	private String mac;
	private PauseState pauseState;
	private WorkingState workingState;

	public MachineToolMessage(String mac, PauseState pauseState,
			WorkingState workingState) {
		this.mac = mac;
		this.pauseState = pauseState;
		this.workingState = workingState;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public void setPauseState(PauseState pauseState) {
		this.pauseState = pauseState;
	}

	public void setWorkingState(WorkingState workingState) {
		this.workingState = workingState;
	}

	public String getMac() {
		return mac;
	}

	public PauseState getPauseState() {
		return pauseState;
	}

	public WorkingState getWorkingState() {
		return workingState;
	}
}
