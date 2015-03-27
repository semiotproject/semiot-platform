package ru.semiot.services.devicedirectory;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import java.io.IOException;
import java.io.StringReader;
import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.RiotException;
import static org.junit.Assert.*;
import org.junit.Test;
import ru.semiot.semiot.commons.namespaces.EMTR;
import ru.semiot.semiot.commons.namespaces.HMTR;
import ru.semiot.semiot.commons.namespaces.RDFS;
import ru.semiot.semiot.commons.namespaces.SSN;

public class RegisterListenerTest {

    private static final String PACKAGE = "/ru/semiot/services/devicedirectory/RegisterListenerTest/";

    @Test
    public void testTurtle() throws IOException {
        Model description = ModelFactory.createDefaultModel()
                .read(new StringReader(read("message.ttl")), null, "TURTLE");

        assertNotNull(description);
        assertTrue(description.size() > 0);
    }

    @Test
    public void testNonDefault() throws IOException {
        try {
            Model description = ModelFactory.createDefaultModel()
                    .read(new StringReader(read("message.xml")), null, "TURTLE");
            fail();
        } catch (RiotException ex) {
            //As expected
        }
    }

    @Test
    public void testEmpty() throws IOException {
        Model description = ModelFactory.createDefaultModel()
                .read(new StringReader(""), null, "TURTLE");

        assertNotNull(description);
        assertTrue(description.isEmpty());
    }
    
    @Test
    public void testInference() {
        Model schema = FileManager.get().loadModel(SSN.URI);
        schema.add(FileManager.get().loadModel(EMTR.URI));
        schema.add(FileManager.get().loadModel(HMTR.URI));
        
        assertNotNull(schema);
        
        InfModel model = ModelFactory.createRDFSModel(schema);
        
        assertTrue(model.contains(EMTR.ElectricMeter, RDFS.subClassOf, SSN.Sensor));
    }

    private String read(final String name) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream(
                PACKAGE + name));
    }
}
