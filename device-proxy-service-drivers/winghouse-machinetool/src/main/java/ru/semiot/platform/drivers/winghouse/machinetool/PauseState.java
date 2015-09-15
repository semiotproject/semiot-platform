package ru.semiot.platform.drivers.winghouse.machinetool;

public enum PauseState {
	ADJUSTMENT(1, "Adjustment"), MAINTENCE(2, "Maintenance"), CONTROLDETAIL(4, "ControlDetail"), 
	REPLACEMENTDETAIL(8, "ReplacementDetail"), HAVENOTMATERIAL(16, "HaveNotMaterial"), 
	HAVENOTPROGRAM(32, "HaveNotProgram"), UNKNOWN(0, "Unknown");

    PauseState(int index, String uri) {
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

    public static PauseState get(int index) {
    	for( PauseState ps : values() ) {
    		if(ps.getIndex() == index) {
    			return ps;
    		}
    	}
    	return UNKNOWN;
    }
}
