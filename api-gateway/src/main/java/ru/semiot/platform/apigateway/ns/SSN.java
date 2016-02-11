package ru.semiot.platform.apigateway.ns;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class SSN {

    public static final String NS = "http://purl.oclc.org/NET/ssnx/ssn#";
    
    public static final Resource System = ResourceFactory.createResource(NS + "System");
    public static final Resource SensingDevice = ResourceFactory.createResource(NS + "SensingDevice");
    
}
