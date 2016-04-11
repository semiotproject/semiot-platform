package ru.semiot.services.tsdbservice.rest;

import org.glassfish.jersey.server.ResourceConfig;

public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        packages("ru.semiot.services.tsdbservice.rest");

        register(ru.semiot.commons.restapi.ZoneDateTimeProvider.class);
    }
}
