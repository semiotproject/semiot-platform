package rs.proxy.service;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

public class JerseyApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> result = new HashSet<Class<?>>();
        result.add(StatusResource.class);
        result.add(RemoveServiceImpl.class);
        return result;
    }

}