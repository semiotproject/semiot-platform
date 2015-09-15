package ru.semiot.platform.drivers.winghouse.machinetool;

public enum WorkingState {
    ENERGIZE(1, "Energize"), INWORK(3, "InWork"), CRASH(4, "Crash"), UNKNOWN(0, "UnknownState");

    WorkingState(int index, String uri) {
    	this.index = index;
    	this.uri = uri;
    }

    private String uri;
    private int index;

    public int getIndex() {
        return index;
    }
    
    public String getUri() {
        return uri;
    }

    public static WorkingState get(int index) {
    	for( WorkingState ws : values() ) {
    		if(ws.getIndex() == index) {
    			return ws;
    		}
    	}
    	return UNKNOWN;
    }
}