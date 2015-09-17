package ru.semiot.platform.drivers.winghouse.machinetool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;

import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

public class DeviceDriverImpl implements DeviceDriver {

	private volatile DeviceManager deviceManager;

	private String templateDescription;
	private String templateButtonsObservation;
	private String templateWorkingStateObservation;
	private List<Device> listDevices = Collections.synchronizedList(new ArrayList<Device>());

	public List<Device> listDevices() {
		return listDevices;
	}

	public void start() {
		System.out.println("Winghouse machine-tools driver started!");

		readTemplates();

		runDeviceHandler();
	}

	public void stop() {
		System.out.println("Winghouse machine-tools driver stopped!");
		// s.cancel(true);
	}

	public void publish(String topic, String message) {
		deviceManager.publish(topic, message);
	}

	public void register(Device device) {
		deviceManager.register(device);
	}

	public boolean contains(Device device) {
		return listDevices.contains(device);
	}

	public String getTemplateDescription() {
		return templateDescription;
	}

	public String getTemplateButtonsObservation() {
		return templateButtonsObservation;
	}

	public String getTemplateWorkingStateObservation() {
		return templateWorkingStateObservation;
	}

	private void readTemplates() {
		try {
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

	public void runDeviceHandler() {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioDatagramChannel.class)
					.option(ChannelOption.SO_BROADCAST, true)
					.handler(new DeviceHandler(this));

			b.bind(9500).sync().channel().closeFuture().await();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}
}
