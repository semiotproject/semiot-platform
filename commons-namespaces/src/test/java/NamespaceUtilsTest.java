
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;
import static org.junit.Assert.*;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.commons.namespaces.NamespaceUtils;
import ru.semiot.commons.namespaces.SHACL;

import java.net.URI;

public class NamespaceUtilsTest {

    @Test
    public void testToSPARQLHeader() {
        String actual = NamespaceUtils.toSPARQLPrologue(
                Hydra.class, OWL.class, SHACL.class);

        String expected = "PREFIX hydra: <http://www.w3.org/ns/hydra/core#>\n"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "PREFIX sh: <http://www.w3.org/ns/shacl#>\n";

        assertEquals(expected, actual);
    }

    @Test
    public void testToSPARQLHeaderWithJenaNamespaces() {
        String actual = NamespaceUtils.toSPARQLPrologue(Hydra.class, RDF.class);

        String expected = "PREFIX hydra: <http://www.w3.org/ns/hydra/core#>\n"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";

        assertEquals(expected, actual);
    }

    @Test
    public void testExtractLocalName() {
        String name1 = NamespaceUtils.extractLocalName(
                URI.create("http://example.com/123"));
        assertEquals("123", name1);

        String name2 = NamespaceUtils.extractLocalName(
                URI.create("http://example.com#123"));
        assertEquals("123", name2);

        String name3 = NamespaceUtils.extractLocalName(
                URI.create("http://example.com#"));
        assertEquals(null, name3);

        String name4 = NamespaceUtils.extractLocalName(
                URI.create("http://example.com/"));
        assertEquals(null, name4);

        String name5 = NamespaceUtils.extractLocalName(
                URI.create("http://example.com"));
        assertEquals(null, name5);
    }

}
