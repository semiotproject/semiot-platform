package ru.semiot.platform.apigateway;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import ru.semiot.platform.apigateway.utils.RDFUtils;

public class RDFMatcherTest {
    
    private final String message;

    public RDFMatcherTest() throws IOException {
        this.message = IOUtils.toString(
                this.getClass().getResourceAsStream("/RDFMatcherTest/message.ttl"));
    }
    
    @Test
    public void matchAny() throws IOException {
        assertTrue(RDFUtils.match(message, Node.ANY, Node.ANY, Node.ANY));
    }
    
    @Test
    public void matchPredicateAndObject() throws IOException {
        assertTrue(RDFUtils.match(message, 
                Node.ANY, 
                ResourceFactory.createProperty("http://purl.oclc.org/NET/ssnx/ssn#observedBy").asNode(), 
                ResourceFactory.createResource("http://localhost/sensors/327956808-1").asNode()));
        
        assertFalse(RDFUtils.match(message, 
                Node.ANY, 
                ResourceFactory.createProperty("http://purl.oclc.org/NET/ssnx/ssn#observedBy").asNode(), 
                ResourceFactory.createResource("http://localhost/sensors/327956808-2").asNode()));
    }
    
}
