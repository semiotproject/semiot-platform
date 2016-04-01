package ru.semiot.services.tsdbservice;

import java.io.IOException;

import org.aeonbits.owner.ConfigFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.semiot.services.tsdbservice.rest.Observations;
import ru.semiot.services.tsdbservice.wamp.RemoveListener;
import ru.semiot.services.tsdbservice.wamp.SubscribeListener;
import ru.semiot.services.tsdbservice.wamp.WAMPClient;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;

public class Launcher {
	private static final Logger logger = LoggerFactory
			.getLogger(Launcher.class);
	private static final ServiceConfig config = ConfigFactory
			.create(ServiceConfig.class);

	public static void main(String[] args) {
		Launcher launcher = new Launcher();
		launcher.run();
	}

	private void run() {
		try {
			WAMPClient
					.getInstance()
					.init()
					.subscribe(
							(WampClient.Status newStatus) -> {
								if (newStatus == WampClient.Status.Connected) {
									logger.info("Connected to {}",
											config.wampUri());

									WAMPClient
											.getInstance()
											.subscribe(
													config.topicsSubscriber())
											.subscribe(new SubscribeListener());

									// WriterOpenTsdb.getInstance().start();

									WAMPClient
											.getInstance()
											.subscribe(
													config.topicsRemoveSensor())
											.subscribe(new RemoveListener());
								} else if (newStatus == WampClient.Status.Disconnected) {
									logger.info("Disconnected from {}",
											config.wampUri());
								} else if (newStatus == WampClient.Status.Connecting) {
									logger.debug("Connecting to {}",
											config.wampUri());
								}
							});

			TSDBClient.getInstance().init();
			
			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
	        context.setContextPath("/");
	 
	        Server jettyServer = new Server(8787);
	        jettyServer.setHandler(context);
	 
	        ServletHolder jerseyServlet = context.addServlet(
	             org.glassfish.jersey.servlet.ServletContainer.class, "/*");
	        jerseyServlet.setInitOrder(0);
	 
	        // Tells the Jersey Servlet which REST service/class to load.
	        jerseyServlet.setInitParameter(
	           "jersey.config.server.provider.classnames",
	           Observations.class.getCanonicalName());
	 
	        try {
	            jettyServer.start();
	            jettyServer.join();
	        } finally {
	            jettyServer.destroy();
	        }
						
			synchronized (this) {
				while (!Thread.interrupted()) {
					wait();
				}
			}
		} catch (ApplicationError | InterruptedException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				TSDBClient.getInstance().stop();
				WAMPClient.getInstance().close();
			} catch (IOException ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}
}
