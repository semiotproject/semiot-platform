package ru.semiot.platform.drivers.winghouse.machinetool;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import io.netty.channel.socket.DatagramPacket;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;

public class DeviceHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private final DeviceDriverImpl deviceDriverImpl;

	private static final String templateTopic = "${MAC}.machinetool.obs";

	public DeviceHandler(DeviceDriverImpl deviceDriverImpl) {
		System.out.println("Create DeviceHandler class");
		this.deviceDriverImpl = deviceDriverImpl;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, DatagramPacket arg1)
			throws Exception {
		int numReadBytes = arg1.content().readableBytes();
		byte[] conBytes = new byte[numReadBytes];
		arg1.content().readBytes(conBytes);
		processingPacket(conBytes);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
	}

	private void processingPacket(byte[] res) {
		MachineToolMessage mess = MessageParser.parsePacket(res);

		if (!deviceDriverImpl.contains(new Device(templateTopic.replace(
				"${MAC}", mess.getMac()), ""))) {
			System.out.println(mess.getMac() + " not exist");
			addDevice(mess.getMac());
		}

		sendMessage(mess, System.currentTimeMillis());
	}

	private void sendMessage(MachineToolMessage mess, long timestamp) {

		String topic = templateTopic.replace("${MAC}", mess.getMac());

		final String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
				.format(new Date(timestamp));

		System.out.println("StateButtonsObservation "
				+ mess.getPauseState().getUri());
		String messageButtons = deviceDriverImpl
				.getTemplateButtonsObservation()
				.replace("${MAC}", mess.getMac())
				.replace("${TIMESTAMP}", String.valueOf(timestamp))
				.replace("${DATETIME}", date)
				.replace("${STATE}",
						String.valueOf(mess.getPauseState().getUri()));

		System.out.println("StateWorkingObservation "
				+ mess.getWorkingState().getUri());
		String messageWorkingState = deviceDriverImpl
				.getTemplateWorkingStateObservation()
				.replace("${MAC}", mess.getMac())
				.replace("${TIMESTAMP}", String.valueOf(timestamp))
				.replace("${DATETIME}", date)
				.replace("${STATE}",
						String.valueOf(mess.getWorkingState().getUri()));

		deviceDriverImpl.publish(topic, messageButtons);
		deviceDriverImpl.publish(topic, messageWorkingState);
	}

	private void addDevice(String mac) {
		// инициализация нового девайса
		String message = deviceDriverImpl.getTemplateDescription().replace(
				"${MAC}", mac);
		System.out.println("Publish message:\n" + message);
		deviceDriverImpl.addDevice(new Device(templateTopic.replace("${MAC}",
				mac), message));
	}
}
