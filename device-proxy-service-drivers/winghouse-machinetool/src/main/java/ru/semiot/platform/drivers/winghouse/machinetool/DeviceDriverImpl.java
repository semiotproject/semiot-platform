package ru.semiot.platform.drivers.winghouse.machinetool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.util.ArrayList;
import java.util.List;

import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

public class DeviceDriverImpl implements DeviceDriver {
    
    private volatile DeviceManager deviceManager;

    public List<Device> listDevices() {
        System.out.println("listDevices is called!");
        return new ArrayList<Device>();
    }
    
    public void start() {
        System.out.println("Winghouse machine-tools driver started!");
		runDeviceHandler(deviceManager);
    }
    
    public void stop() {	
        System.out.println("Winghouse machine-tools driver stopped!");      
        //s.cancel(true);
    }
    
    public void runDeviceHandler(DeviceManager deviceManager) {
    	EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             .option(ChannelOption.SO_BROADCAST, true)
             .handler(new DeviceHandler(deviceManager));
            
            b.bind(9500).sync().channel().closeFuture().await();
        } catch (Exception e) {
        	System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
            group.shutdownGracefully();
        }
    }
}
