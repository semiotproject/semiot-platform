package ru.semiot.platform.deviceproxyservice.launcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.Bundle;


public class Launcher {
	
	public static void main(String args[]) throws BundleException {
		
		ServiceLoader<FrameworkFactory> loader=ServiceLoader.load(FrameworkFactory.class);
		FrameworkFactory frameworkFactory=loader.iterator().next();

		
		Map<String, String> config = new HashMap<String, String>();
		
		Framework framework = frameworkFactory.newFramework(config);
		
		framework.start();
		
		BundleContext context = framework.getBundleContext();
		
		List<Bundle> bundles = new ArrayList<Bundle>();
		
		
		//bundles.add(context.installBundle(""));	
		
		bundles.add(context.installBundle("http://apache-mirror.rbc.ru/pub/apache//felix/org.apache.felix.configadmin-1.8.8.jar"));		
		bundles.add(context.installBundle("http://apache-mirror.rbc.ru/pub/apache//felix/org.apache.felix.log-1.0.1.jar"));
		bundles.add(context.installBundle("http://apache-mirror.rbc.ru/pub/apache//felix/org.apache.felix.metatype-1.1.2.jar"));
		// maven
		bundles.add(context.installBundle("http://central.maven.org/maven2/org/apache/felix/org.apache.felix.dependencymanager/4.1.0/org.apache.felix.dependencymanager-4.1.0.jar"));
		
		// ВОПРОС КАКИЕ ИЗ НИХ НАМ НЕОБХОДИМЫ, закончил на sun.security.util bundle
		bundles.add(context.installBundle("http://central.maven.org/maven2/com/google/protobuf/protobuf-java/2.6.1/protobuf-java-2.6.1.jar"));
		bundles.add(context.installBundle("http://central.maven.org/maven2/org/apache/servicemix/bundles/org.apache.servicemix.bundles.jzlib/1.1.3_2/org.apache.servicemix.bundles.jzlib-1.1.3_2.jar"));
		bundles.add(context.installBundle("http://central.maven.org/maven2/org/apache/servicemix/bundles/org.apache.servicemix.bundles.javassist/3.12.1.ga_1/org.apache.servicemix.bundles.javassist-3.12.1.ga_1.jar"));
		
		bundles.add(context.installBundle("http://central.maven.org/maven2/commons-logging/commons-logging/1.1.3/commons-logging-1.1.3.jar"));
		bundles.add(context.installBundle("http://central.maven.org/maven2/log4j/log4j/1.2.17/log4j-1.2.17.jar"));
		
		context.installBundle("http://ebr.springsource.com/repository/app/bundle/version/download?name=com.springsource.org.apache.coyote&version=7.0.26&type=binary");
		bundles.add(context.installBundle("http://central.maven.org/maven2/org/apache/servicemix/bundles/org.apache.servicemix.bundles.bcpg-jdk16/1.46_2/org.apache.servicemix.bundles.bcpg-jdk16-1.46_2.jar"));
		bundles.add(context.installBundle("http://ebr.springsource.com/repository/app/bundle/version/download?name=bcmail&version=1.46.0&type=binary"));
		bundles.add(context.installBundle("http://central.maven.org/maven2/org/eclipse/jetty/osgi/jetty-osgi-npn/9.2.10.v20150310/jetty-osgi-npn-9.2.10.v20150310.jar"));
		bundles.add(context.installBundle("http://central.maven.org/maven2/org/jboss/marshalling/jboss-marshalling-osgi/1.4.10.Final/jboss-marshalling-osgi-1.4.10.Final.jar"));
		context.installBundle("http://central.maven.org/maven2/org/slf4j/slf4j-simple/1.7.12/slf4j-simple-1.7.12.jar");
		bundles.add(context.installBundle("http://central.maven.org/maven2/org/slf4j/slf4j-api/1.7.12/slf4j-api-1.7.12.jar"));
		bundles.add(context.installBundle("http://central.maven.org/maven2/com/github/livesense/org.liveSense.fragment.sun.misc/1.0.5/org.liveSense.fragment.sun.misc-1.0.5.jar"));
		/*bundles.add(context.installBundle("http://central.maven.org/maven2/org/glassfish/javax.annotation/3.1.1/javax.annotation-3.1.1.jar"));
		bundles.add(context.installBundle("http://ebr.springsource.com/repository/app/bundle/version/download?name=javax.servlet&version=3.0.0.v201103241009&type=binary"));
		bundles.add(context.installBundle("http://central.maven.org/maven2/javax/xml/jaxrpc-api-osgi/1.1-b01/jaxrpc-api-osgi-1.1-b01.jar"));
		bundles.add(context.installBundle("http://ebr.springsource.com/repository/app/bundle/version/download?name=com.springsource.javax.ejb&version=3.0.0&type=binary"));
		bundles.add(context.installBundle("http://ebr.springsource.com/repository/app/bundle/version/download?name=com.springsource.javax.el&version=1.0.0&type=binary"));
		// очень много подтянулось за ней, чистого jni не нашел
		bundles.add(context.installBundle("http://central.maven.org/maven2/org/ow2/jonas/jonas-web-container-tomcat-7.0-core/5.3.0/jonas-web-container-tomcat-7.0-core-5.3.0.jar"));*/
		//bundles.add(context.installBundle(""));
		
		//String str = Launcher.class.getClassLoader().getResource("/ru/semiot/bundles/device-proxy-service-api-1.0-SNAPSHOT.jar").getPath();
		//System.out.println(str);
		// resource
		//bundles.add(context.installBundle("/ru/semiot/bundles/device-proxy-service-api-1.0-SNAPSHOT.jar"));
		//String str = Launcher.class.getClassLoader().getResource("ru/semiot/bundles/device-proxy-service-api-1.0-SNAPSHOT.jar").getPath();
		//System.out.println(str);
		bundles.add(context.installBundle("device-proxy-service-api-1.0-SNAPSHOT.jar", Launcher.class.getClassLoader().getResourceAsStream("ru/semiot/bundles/device-proxy-service-api-1.0-SNAPSHOT.jar")));
		bundles.add(context.installBundle("device-proxy-service-manager-1.0-SNAPSHOT.jar", Launcher.class.getClassLoader().getResourceAsStream("ru/semiot/bundles/device-proxy-service-manager-1.0-SNAPSHOT.jar")));
		bundles.add(context.installBundle("winghouse-machinetool-1.0-SNAPSHOT.jar", Launcher.class.getClassLoader().getResourceAsStream("ru/semiot/bundles/winghouse-machinetool-1.0-SNAPSHOT.jar")));
		
		
		for (Bundle bundle : bundles) {
			bundle.start();
		}
	}
	
}

