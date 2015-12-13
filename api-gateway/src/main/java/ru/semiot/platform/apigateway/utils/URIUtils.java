package ru.semiot.platform.apigateway.utils;

import java.net.URI;

public class URIUtils {
    
    private static final String COLON = ":";
    private static final String PROTOCOL_SEPARATOR = "://";
    
    public static final String extractHostName(URI uri) {
        final StringBuilder builder = new StringBuilder(uri.getScheme())
                .append(PROTOCOL_SEPARATOR).append(uri.getHost());
        
        if(uri.getPort() != 80 && uri.getPort() != -1) {
            builder.append(COLON).append(uri.getPort());
        }
        return builder.toString();
    }
    
}
