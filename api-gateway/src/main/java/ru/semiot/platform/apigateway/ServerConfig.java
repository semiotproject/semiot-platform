package ru.semiot.platform.apigateway;

import org.aeonbits.owner.Config;

public interface ServerConfig extends Config {
    
    @DefaultValue("http://127.0.0.1:3030/ds/query")
    String sparqlEndpoint();
    
    @DefaultValue("admin")
    String sparqlUsername();
    
    @DefaultValue("pw")
    String sparqlPassword();
    
    @DefaultValue("ws://127.0.0.1:8080/ws")
    String wampUri();
    
    @DefaultValue("realm1")
    String wampRealm();
    
}
