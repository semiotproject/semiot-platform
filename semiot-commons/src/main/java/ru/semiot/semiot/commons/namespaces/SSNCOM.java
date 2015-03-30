package ru.semiot.semiot.commons.namespaces;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class SSNCOM extends Namespace {
    
    public static final String URI = "http://purl.org/NET/ssnext/communication#";
    
    public static final Property hasCommunicationEndpoint = property(URI, "hasCommunicationEndpoint");
    public static final Property protocol = property(URI, "protocol");
    
    public static final Resource CommunicationEndpoint = resource(URI, "CommunicationEndpoint");
    
}
