/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.semiot.configprototype;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.felix.webconsole.AbstractWebConsolePlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
public class SimpleServlet extends AbstractWebConsolePlugin {

    public static final String TITLE = "simpleServlet";
    public static final String LABEL = "hello";
    BundleContext context;

    public SimpleServlet(BundleContext bundle) {
        super();
        this.context = bundle;
    }

    @Override
    protected void renderContent(HttpServletRequest hsr, HttpServletResponse hsr1) throws ServletException, IOException {
        String PID = "ru.semiot.platform.drivers.netatmo-weatherstation";        
        BundleContext ctx = context;        
        Bundle[] bundles = ctx.getBundles();
        for (Bundle bun : bundles){
            if(bun.getSymbolicName().contains(PID)){
                ctx = bun.getBundleContext();
                break;
            }
        }    
        ServiceReference configurationAdminReference
                = ctx.getServiceReference(ConfigurationAdmin.class.getName());
        if (configurationAdminReference != null) {
            ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) ctx.getService(configurationAdminReference);
            Configuration config = (Configuration) configurationAdmin.getConfiguration(PID);
            Dictionary props = config.getProperties();            
            if (props == null) {
                
                props = new Hashtable();
            } 

            props.put("Hello", "world");  
            config.update(props);
        }
    }

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

}
