package ru.semiot.platform.drivers.winghouse.machinetool;

public enum MachineToolState {
	IS_TURNED_OFF(0, 0, "IsTurnedOff"), // не подключено 0 любое
	IS_IN_EXECUTION_OF_TASK(3, 0, "IsInExecutionOfTask"), // выполнение работы 3 0 
	IS_OUT_OF_COMMISSION(4, 0, "IsOutOfCommission"), // аварийное состояние 4  любое
	IS_UNDER_SETTING_UP(1, 1, "IsUnderSettingUp"), // наладка 1 1
	IS_UNDER_MAINTENANCE(1, 2, "IsUnderMaintenance"), // тех обслуживание 1 2
	IS_UNDER_INSPECTION_OF_PRODUCT(1, 4, "IsUnderInspectionOfProduct"), //контроль детали 1 4
	IS_UNDER_CHANGING_OF_PRODUCT(1, 8, "IsUnderChangingOfProduct"),// смена детали 1 8
	IS_OUT_OF_MATERIAL(1, 16, "IsOutOfMaterial"),// нет материала 1 16
	IS_OUT_OF_PROGRAM(1, 32, "IsOutOfProgram");// нет программы 1 32
	// UNKNOWN(-1, -1, "Unknown"); // неизвестно

    MachineToolState(int pauseValue, int workingValue, String uri) {
    	this.pauseValue = pauseValue;
    	this.workingValue = workingValue;
    	this.uri = uri;
    }

    private final String uri;
    private final int pauseValue;
    private final int workingValue;

    public int getPauseValue() {
        return pauseValue;
    }
    
    public int getWorkingValue() {
        return workingValue;
    }
    
    public String getUri() {
        return uri;
    }

    public static MachineToolState get(int pauseValue, int workingValue) {
    	if(pauseValue == 0) {
    		return IS_TURNED_OFF;
    	}
    	if(pauseValue == 4) {
    		return IS_OUT_OF_COMMISSION;
    	}
    	for( MachineToolState ps : values() ) {
    		if(ps.getPauseValue() == pauseValue && ps.getWorkingValue() == workingValue) {
    			return ps;
    		}
    	}
    	return null;
    }
}
