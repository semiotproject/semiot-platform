package ru.semiot.platform.drivers.winghouse.machinetool;

public class MessageParser {

	public static MachineToolValue parsePacket(byte[] res) {
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
		// uint16_t CaseOfTransmission;
		// 0x1 - timer 0x2 - buttons 0x4 - inputs 0x8 - restart
		// int caseOfTransmission = getUInt16_t(res, index);
		//System.out.println("CaseOfTransmission " + String.valueOf(caseOfTransmission));
		// uint8_t StateOfButtons;
		int stateOfButtons = getUInt8_t(res[index += 6]);
		System.out.println("StateOfButtons " + String.valueOf(stateOfButtons));
		// uint8_t StateOfInputs;
		int stateOfInputs = getUInt8_t(res[index += 1]);
		System.out.println("StateOfInputs " + String.valueOf(stateOfInputs));

		return new MachineToolValue(mac, MachineToolState.get(stateOfInputs, stateOfButtons), System.currentTimeMillis());
	}

	private static int getUInt8_t(byte value) {
		return ((int) value) & 0x000000FF;
	}

	private static int getUInt16_t(byte[] value, int index) {
		int res = -1;
		if (value.length - index > 1) {
			int first = ((int) (value[index++])) & 0x000000FF;
			int second = ((int) (value[index])) & 0x000000FF;
			res = ((int) first << 8 | second) & 0xFFFF;
		}
		return res;
	}

}
