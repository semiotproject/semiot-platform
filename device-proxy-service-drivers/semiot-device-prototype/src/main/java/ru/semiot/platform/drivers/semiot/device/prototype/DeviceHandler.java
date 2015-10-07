package ru.semiot.platform.drivers.semiot.device.prototype;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import io.netty.channel.socket.DatagramPacket;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;

public class DeviceHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private final DeviceDriverImpl deviceDriverImpl;

	private static final String templateTopic = "${MAC}.prototype.obs";

	public DeviceHandler(DeviceDriverImpl deviceDriverImpl) {
		System.out.println("Create DeviceHandler class");
		this.deviceDriverImpl = deviceDriverImpl;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, DatagramPacket arg1)
			throws Exception {
		System.out.println("Read packet");
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
		DevicePrototypeMessage mess = MessageParser.parsePacket(res);

		if (!deviceDriverImpl.contains(new Device(templateTopic.replace(
				"${MAC}", mess.getMac()), ""))) {
			System.out.println(mess.getMac() + " not exist");
			addDevice(mess.getMac());
		}

		sendMessage(mess, System.currentTimeMillis());
	}

	private void sendMessage(DevicePrototypeMessage mess, long timestamp) {

		String topic = templateTopic.replace("${MAC}", mess.getMac());

		final String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
				.format(new Date(timestamp));

		System.out.println("Temperature "
				+ mess.getTemperature());
		String messageTemperature = deviceDriverImpl
				.getTemplateTemperatureObservation()
				.replace("${MAC}", mess.getMac())
				.replace("${TIMESTAMP}", String.valueOf(timestamp))
				.replace("${DATETIME}", date)
				.replace("${STATE}",
						String.valueOf(mess.getTemperature()));

		deviceDriverImpl.publish(topic, messageTemperature);
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
