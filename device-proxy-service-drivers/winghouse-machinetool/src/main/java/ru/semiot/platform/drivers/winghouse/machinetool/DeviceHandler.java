package ru.semiot.platform.drivers.winghouse.machinetool;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.netty.channel.socket.DatagramPacket;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;

public class DeviceHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private final DeviceDriverImpl deviceDriverImpl;

	private static final String templateTopic = "${DEVICE_HASH}";
	private static final String templateOnState = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "<http://${DOMAIN}/${PATH}/${DEVICE_HASH}> saref:hasState saref:OnState.";

	public DeviceHandler(DeviceDriverImpl deviceDriverImpl) {
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
		MachineToolValue mvalue = MessageParser.parsePacket(res).calculateHash(deviceDriverImpl.getDriverName());
		if (!deviceDriverImpl.contains(new Device( mvalue.getHash(), ""))) {
			System.out.println(mvalue.getHash() + " not exist");
			addDevice(mvalue.getHash());
			sendMessage(mvalue);
			deviceDriverImpl.getOldValueMachineTools().put(mvalue.getHash(), mvalue); // поправить?
		} else {
			if(deviceDriverImpl.getOldValueMachineTools().containsKey(mvalue.getHash())) {
				if(!deviceDriverImpl.getOldValueMachineTools().get(mvalue.getHash()).equals(mvalue)) {
					sendMessage(mvalue);
				} 
				if(!deviceDriverImpl.getOldValueMachineTools().get(mvalue.getHash()).getTurnOn()) {
					deviceDriverImpl.inactiveDevice(templateOnState.replace("${DOMAIN}", deviceDriverImpl.getDomain())
							.replace("${PATH}", deviceDriverImpl.getPathSystemUri()).replace("${DEVICE_HASH}", mvalue.getHash()));
					// deviceDriverImpl.getOldStateMachineTools().get(mess.getMac()).setTurnOn(true);
					System.out.println(mvalue.getHash() + " saref:OnState" );
				}
				deviceDriverImpl.getOldValueMachineTools().replace(mvalue.getHash(), mvalue);
			} else {
				deviceDriverImpl.getOldValueMachineTools().put(mvalue.getHash(), mvalue);
				sendMessage(mvalue);
			}
		}
	}

	private void sendMessage(MachineToolValue mvalue) {
		if(mvalue.getMachineToolState() != null) {
			String topic = templateTopic.replace("${DEVICE_HASH}", mvalue.getHash());
	
			final String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
					.format(new Date(mvalue.getTimestemp()));
	
			// System.out.println("State " + mess.getMachineToolState().getUri());
			String message = deviceDriverImpl
					.getTemplateObservation()
					.replace("${DOMAIN}", deviceDriverImpl.getDomain())
					.replace("${PATH}", deviceDriverImpl.getPathSystemUri())
					.replace("${DEVICE_HASH}", mvalue.getHash())
					.replace("${TIMESTAMP}", String.valueOf(mvalue.getTimestemp()))
					.replace("${DATETIME}", date)
					.replace("${STATE}",
							String.valueOf(mvalue.getMachineToolState().getUri()));
	
			deviceDriverImpl.publish(topic, message);
		} else {
			System.err.println(String.valueOf(mvalue.getHash()) + " has unknown state (null)");
		}
	}

	private void addDevice(String hashDevice) {
		// инициализация нового девайса
		String message = deviceDriverImpl.getTemplateDescription().replace(
				"${DEVICE_HASH}", hashDevice).replace("${PATH}", deviceDriverImpl.getPathSystemUri())
				.replace("${DOMAIN}", deviceDriverImpl.getDomain());
		// System.out.println("Publish message:\n" + message);
		deviceDriverImpl.addDevice(new Device(hashDevice, message));
	}
}
