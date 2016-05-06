package ru.semiot.commons.namespaces;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class SSNCOM extends Namespace {
    
    public static final String URI = "http://purl.org/NET/ssnext/communication#";
    
    public static final Property hasCommunicationEndpoint = property(URI, "hasCommunicationEndpoint");
    public static final Property protocol = property(URI, "protocol");
    public static final Property topic = property(URI, "topic");
    public static final Property provide = property(URI, "provide");
    
    public static final Resource CommunicationEndpoint = resource(URI, "CommunicationEndpoint");
    
}
