package ru.semiot.services.devicedirectory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.IOException;
import java.io.StringReader;
import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.RiotException;
import static org.junit.Assert.*;
import org.junit.Test;

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

    private String read(final String name) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream(
                PACKAGE + name));
    }
}
