package ru.semiot.platform.apigateway.rest;

import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(ru.semiot.platform.apigateway.rest.RootResource.class);
        resources.add(ru.semiot.platform.apigateway.rest.SensorResource.class);
        resources.add(ru.semiot.platform.apigateway.rest.SubSystemResource.class);
        resources.add(ru.semiot.platform.apigateway.rest.SystemActuationsResource.class);
        resources.add(ru.semiot.platform.apigateway.rest.SystemObservationsResource.class);
        resources.add(ru.semiot.platform.apigateway.rest.SystemResource.class);
        resources.add(ru.semiot.platform.apigateway.rest.providers.ModelMessageBodyReader.class);
    }
    
}
