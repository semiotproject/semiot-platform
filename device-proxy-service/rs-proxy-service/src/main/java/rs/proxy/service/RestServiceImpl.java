package rs.proxy.service;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;


public class RestServiceImpl implements ManagedService {
	
	private volatile DeviceManager deviceManager;
	
	public RestServiceImpl() {
		RestServiceImpl.instanceRS = this;
	}

	private static RestServiceImpl instanceRS ;
	
	private BundleContext bc;
	private ServiceTracker tracker;
	private HttpService httpService = null;
	private static final Logger logger = Logger.getLogger(RestServiceImpl.class
			.getName());
	
	public static RestServiceImpl getInstance() {
		return RestServiceImpl.instanceRS;
	}
	
	public static int removeDataOfDriverFromFuseki(String pid) {
		return instanceRS.getDeviceManager().removeDataOfDriverFromFuseki(pid);
	}
	
	public DeviceManager getDeviceManager() {
		return deviceManager;
	}
	
	public synchronized void start()
			throws Exception {

		this.bc = FrameworkUtil.getBundle(RestServiceImpl.class).getBundleContext();

		logger.info("Starting HTTP service bundle");

		this.tracker = new ServiceTracker(this.bc, HttpService.class.getName(),
				null) {

			@Override
			public Object addingService(ServiceReference serviceRef) {
				httpService = (HttpService) super.addingService(serviceRef);
				registerServlets();
				return httpService;
			}

			@Override
			public void removedService(ServiceReference ref, Object service) {
				if (httpService == service) {
					unregisterServlets();
					httpService = null;
				}
				super.removedService(ref, service);
			}
		};

		this.tracker.open();

		logger.info("HTTP service bundle started");
	}

	public synchronized void stop() throws Exception {
		this.tracker.close();
	}

	private void registerServlets() {
		try {
			rawRegisterServlets();
		} catch (Exception ie) {
			throw new RuntimeException(ie);
		}
	}

	private void rawRegisterServlets() throws ServletException,
			NamespaceException, InterruptedException {
		logger.info("JERSEY BUNDLE: registring servlets");
		logger.info("JERSEY BUNDLE: HTTP service = " + httpService.toString());

		// TODO - temporary workaround
		// This is a workaround related to issue JERSEY-2093; grizzly (1.9.5)
		// needs to have the correct context
		// classloader set
		ClassLoader myClassLoader = getClass().getClassLoader();
		ClassLoader originalContextClassLoader = Thread.currentThread()
				.getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(myClassLoader);
			httpService.registerServlet("/jersey-http-service",
					new ServletContainer(), getJerseyServletParams(), null);
		} finally {
			Thread.currentThread().setContextClassLoader(
					originalContextClassLoader);
		}

		sendAdminEvent();
		logger.info("JERSEY BUNDLE: servlets registered");
	}

	private void sendAdminEvent() {
		ServiceReference eaRef = bc.getServiceReference(EventAdmin.class
				.getName());
		if (eaRef != null) {
			EventAdmin ea = (EventAdmin) bc.getService(eaRef);
			ea.sendEvent(new Event("jersey/test/DEPLOYED",
					new HashMap<String, String>() {
						{
							put("context-path", "/");
						}
					}));
			bc.ungetService(eaRef);
		}
	}

	private void unregisterServlets() {
		if (this.httpService != null) {
			logger.info("JERSEY BUNDLE: unregistered servlets");
			httpService.unregister("/jersey-http-service");
			logger.info("JERSEY BUNDLE: servlets unregistered");
		}
	}

	@SuppressWarnings("UseOfObsoleteCollectionType")
	private Dictionary<String, String> getJerseyServletParams() {
		Dictionary<String, String> jerseyServletParams = new Hashtable<>();
		jerseyServletParams.put("javax.ws.rs.Application",
				JerseyApplication.class.getName());
		return jerseyServletParams;
	}

	@Override
	public void updated(Dictionary properties) throws ConfigurationException {
		// TODO Auto-generated method stub
		
	}

}
