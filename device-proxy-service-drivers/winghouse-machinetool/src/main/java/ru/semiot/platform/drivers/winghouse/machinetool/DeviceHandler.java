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
	private static final String templateOnState = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "<http://example.com/${MAC}> saref:hasState saref:OnState.";

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
		MachineToolValue mess = MessageParser.parsePacket(res);
		if (!deviceDriverImpl.contains(new Device( mess.getMac(), ""))) {
			System.out.println(mess.getMac() + " not exist");
			addDevice(mess.getMac());
			sendMessage(mess);
			deviceDriverImpl.getOldStateMachineTools().put(mess.getMac(), mess); // поправить?
		} else {
			if(deviceDriverImpl.getOldStateMachineTools().containsKey(mess.getMac())) {
				if(!deviceDriverImpl.getOldStateMachineTools().get(mess.getMac()).equals(mess)) {
					sendMessage(mess);
				} 
				if(!deviceDriverImpl.getOldStateMachineTools().get(mess.getMac()).getTurnOn()) {
					deviceDriverImpl.publish(deviceDriverImpl.getTopicInactive(),
							templateOnState.replace("${MAC}", mess.getMac()));
					System.out.println(mess.getMac() + "saref:OnState" );
				}
				deviceDriverImpl.getOldStateMachineTools().replace(mess.getMac(), mess);
			} else {
				deviceDriverImpl.getOldStateMachineTools().put(mess.getMac(), mess);
			}
		}
	}

	private void sendMessage(MachineToolValue mess) {

		String topic = templateTopic.replace("${MAC}", mess.getMac());

		final String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
				.format(new Date(mess.getTimestemp()));

		System.out.println("State "
				+ mess.getMachineToolState().getUri());
		String message = deviceDriverImpl
				.getTemplateObservation()
				.replace("${MAC}", mess.getMac())
				.replace("${TIMESTAMP}", String.valueOf(mess.getTimestemp()))
				.replace("${DATETIME}", date)
				.replace("${STATE}",
						String.valueOf(mess.getMachineToolState().getUri()));

		deviceDriverImpl.publish(topic, message);
	}

	private void addDevice(String mac) {
		// инициализация нового девайса
		String message = deviceDriverImpl.getTemplateDescription().replace(
				"${MAC}", mac);
		System.out.println("Publish message:\n" + message);
		deviceDriverImpl.addDevice(new Device(mac, message));
	}
}
