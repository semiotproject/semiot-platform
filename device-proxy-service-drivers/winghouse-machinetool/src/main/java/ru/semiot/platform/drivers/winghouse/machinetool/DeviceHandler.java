package ru.semiot.platform.drivers.winghouse.machinetool;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import org.apache.commons.io.IOUtils;

import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

public class DeviceHandler extends SimpleChannelInboundHandler<DatagramPacket> {
	
	private DeviceManager deviceManager;
	
	private String templateDescription;
	private String templateButtonsObservation;
	private String templateWorkingStateObservation;

	private String templateTopic = "${MAC}.machinetool.obs";

	public DeviceHandler(DeviceManager deviceManager) {
		try {
			System.out.println("Create DeviceHandler class");
			this.deviceManager = deviceManager;
			this.templateDescription = IOUtils
					.toString(DeviceHandler.class
							.getResourceAsStream("/ru/semiot/platform/drivers/winghouse/machinetool/descriptionMachineTools.ttl"));
			this.templateButtonsObservation = IOUtils
					.toString(DeviceHandler.class
							.getResourceAsStream("/ru/semiot/platform/drivers/winghouse/machinetool/buttonsObservation.ttl"));
			this.templateWorkingStateObservation = IOUtils
					.toString(DeviceHandler.class
							.getResourceAsStream("/ru/semiot/platform/drivers/winghouse/machinetool/workingStateObservation.ttl"));
		} catch (IOException ex) {
			System.out.println("Cant read templates");
			throw new IllegalArgumentException(ex);
		}
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext arg0, DatagramPacket arg1)
			throws Exception {
		run(arg1);
	}
	
	@Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

	public void run(DatagramPacket dp) {
		System.out.println(dp.content().toString(CharsetUtil.UTF_8));
		int numReadBytes = dp.content().readableBytes ();
		byte[] conBytes = new byte[numReadBytes];
		dp.content().readBytes(conBytes);   
		parsePacket(conBytes);
	}

	private void parsePacket(byte[] res) {
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
		int caseOfTransmission = getUInt16_t(res, index);
		System.out.println("CaseOfTransmission " + String.valueOf(caseOfTransmission));
		// uint8_t StateOfButtons;
		int stateOfButtons = getUInt8_t(res[index += 4]);
		System.out.println("StateOfButtons " + String.valueOf(stateOfButtons));
		// uint8_t StateOfInputs;
		int stateOfInputs = getUInt8_t(res[index += 1]);
		System.out.println("StateOfInputs " + String.valueOf(stateOfInputs));

		if (!deviceManager.containsDeviceId(templateTopic.replace("${MAC}", mac))) { // TODO deviceManager contains
			System.out.println(mac + " not exist");
			addDevice(mac);
		}

		sendMessage(mac, stateOfButtons, stateOfInputs,
				System.currentTimeMillis());
	}

	private void sendMessage(String mac, int stateOfButtons, int stateOfInputs,
			long timestamp) {
		String topic = templateTopic.replace("${MAC}", mac);

		final String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
				.format(new Date(timestamp));

		String stateButtonsObservation = PauseState.get(stateOfButtons)
				.getUri();
		System.out.println("StateButtonsObservation " + stateButtonsObservation);
		String messageButtons = templateButtonsObservation
				.replace("${MAC}", mac)
				.replace("${TIMESTAMP}", String.valueOf(timestamp))
				.replace("${DATETIME}", date)
				.replace("${STATE}", String.valueOf(stateButtonsObservation));

		String stateWorkingObservation = WorkingState.get(stateOfInputs)
				.getUri();
		System.out.println("StateWorkingObservation " + stateWorkingObservation);
		String messageWorkingState = templateWorkingStateObservation
				.replace("${MAC}", mac)
				.replace("${TIMESTAMP}", String.valueOf(timestamp))
				.replace("${DATETIME}", date)
				.replace("${STATE}", String.valueOf(stateWorkingObservation));

		deviceManager.publish(topic, messageButtons);
		deviceManager.publish(topic, messageWorkingState);
	}

	private void addDevice(String mac) {
		// инициализация нового девайса
		String message = templateDescription.replace("${MAC}", mac);
		System.out.println("Publish message:\n" + message);
		deviceManager.register(new Device(templateTopic.replace("${MAC}", mac), message));
	}

	private static int getUInt8_t(byte value) {
		return ((int) value) & 0x000000FF;
	}

	private int getUInt16_t(byte[] value, int index) {
		int res = -1;
		if (value.length - index > 1) {
			int first = ((int) (value[index++])) & 0x000000FF;
			int second = ((int) (value[index])) & 0x000000FF;
			res = ((int) first << 8 | second) & 0xFFFF;
		}
		return res;
	}
}
